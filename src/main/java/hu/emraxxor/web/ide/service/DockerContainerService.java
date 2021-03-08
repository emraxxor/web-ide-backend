package hu.emraxxor.web.ide.service;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.BiFunction;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.PortBinding;

import hu.emraxxor.web.ide.config.UserProperties;
import hu.emraxxor.web.ide.data.type.docker.ContainerStatus;
import hu.emraxxor.web.ide.data.type.docker.DockerContainerCommand;
import hu.emraxxor.web.ide.data.type.docker.DockerContainerElement;
import hu.emraxxor.web.ide.data.type.docker.DockerContainerExecCommand;
import hu.emraxxor.web.ide.data.type.docker.DockerContainerImage;
import hu.emraxxor.web.ide.data.type.docker.consumer.FrameConsumerResultCallback;
import hu.emraxxor.web.ide.data.type.docker.consumer.OutputFrame;
import hu.emraxxor.web.ide.data.type.docker.consumer.ToStringConsumer;
import lombok.SneakyThrows;
import lombok.Synchronized;

/**
 * 
 * @author Attila Barna
 *
 */
@Service
public class DockerContainerService {

	@Autowired
	private DockerClient client;
	
	@Autowired
	private UserProperties userprops;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private ContainerService containerService;
	
	@Autowired
	private ProjectService projectService;
	
	@Synchronized
	public 	List<Container> containers() {
		return client
					.listContainersCmd()
					.withShowSize(true)
  				    .withShowAll(true)
					.withStatusFilter(Arrays.asList("exited","created"))
					.exec();
	}
	
	
	public static boolean available(int port) {
		    if (port < 3500 || port > 65000) 
		    	return false;

		    ServerSocket ss = null;
		    DatagramSocket ds = null;
		    try {
		        ss = new ServerSocket(port);
		        ss.setReuseAddress(true);
		        ds = new DatagramSocket(port);
		        ds.setReuseAddress(true);
		        return true;
		    } catch (IOException e) {
		    } finally {
		        if (ds != null) 
		            ds.close();

		        if (ss != null) {
		            try {
		                ss.close();
		            } catch (IOException e) {}
		        }
		    }
		    return false;
	}
	
	@Synchronized
	private int randomAvailablePort() {
		int port = new Random().nextInt(65000) + 3500;
		while( !available(port) ) 
			port = new Random().nextInt(65000) + 3500;
		
		return port;
	}
	
	
	@Synchronized
	public Boolean stopContainer(DockerContainerElement el) {
		return authorizedCmd(el, ( e,f ) ->  f.stopContainerCmd(e.getContainerId()).exec() ).isEmpty();
	}
	
	
	@Synchronized
	public void killContainer(DockerContainerElement el) {
		client.killContainerCmd(el.getId());
	}
	
	@Synchronized
	@SneakyThrows
	public Optional<Object> exec(DockerContainerExecCommand exec) {
		var project = projectService.find(exec.getId());
		if ( project.isPresent()  ) {
			var container = project.get().getContainer();
			if ( container != null && project.get().getUser().equals(userService.curr()) ) {
				 var containerid = container.getContainerId();
			     var execCreateCmdResponse = client
			    		 						.execCreateCmd(containerid)
			    		 						.withAttachStdout(true)
			    		 						.withAttachStderr(true)
			    		 						.withCmd(exec.getCommand().split(" "))
			    		 						.exec();
			     
			     var stdoutConsumer = new ToStringConsumer();
			     var stderrConsumer = new ToStringConsumer();

			     var callback = new FrameConsumerResultCallback();
			     callback.addConsumer(OutputFrame.OutputType.STDOUT, stdoutConsumer);
			     callback.addConsumer(OutputFrame.OutputType.STDERR, stderrConsumer);
			     
			     client.execStartCmd(execCreateCmdResponse.getId()).exec(callback).awaitCompletion();

			     return Optional.of( new Object[] {
			    		 stdoutConsumer.toString(Charset.defaultCharset()),
			    		 stderrConsumer.toString(Charset.defaultCharset())
			     });
			} 
		}
		return Optional.empty();
	}
	
	@Transactional(value = TxType.MANDATORY)
	private int randomBind() {
		var port = randomAvailablePort();
		
		while( containerService.findByBind(port).isPresent() )
			port = randomAvailablePort();
		
		return port;
	}

	@Synchronized
	public Optional<String> log(DockerContainerElement cmd) {
	    var callback = new FrameConsumerResultCallback();
	    var consumer = new ToStringConsumer();
		authorizedCmd(cmd, ( e,f ) ->  f.logContainerCmd(e.getContainerId()).exec(callback) );
		return Optional.ofNullable( consumer.toString(Charset.defaultCharset()) );
	}
	
	
	public Boolean start(DockerContainerElement cmd) {
		return authorizedCmd(cmd, ( e,f ) ->  f.startContainerCmd(e.getContainerId()).exec() ).isEmpty();
	}
	

	@Synchronized
	private <T extends hu.emraxxor.web.ide.entities.Container> Optional<?> authorizedCmd(DockerContainerElement el, BiFunction<T,DockerClient,?> fn) {
		var curr = userService.curr();
		var container = containerService.findByContainerIdAndUser(el.getId(), curr);
        if ( container.isPresent() ) 
        	return Optional.ofNullable( fn.apply(  (T) container.get() , client ) );
        return Optional.empty();
	}

	
	@Synchronized
	public InspectContainerResponse inspect(DockerContainerElement inspectcmd) {
		return client.inspectContainerCmd(inspectcmd.getId()).exec();
	}
	
	@Synchronized
	@Transactional
	@SneakyThrows
	public 	Optional<CreateContainerResponse> create(DockerContainerCommand command) {
		var dockerImg = DockerContainerImage.findByName(command.getImage());
		var user = userService.current().get();
		var cproject = projectService.find(command.getProjectId());
		
		if ( dockerImg.isPresent() && cproject.isPresent()  && cproject.get().getUser().equals(user) ) {
			CreateContainerResponse resp;
			var project = cproject.get();
			var container = project.getContainer();
			var img = dockerImg.get();
			var uname = user.getNeptunId();
			var userdir = userprops.getStorage();
			var bindport = randomBind();
			var appdir = userdir + "/" + uname +  "/projects/" + project.getIdentifier();
				
			if ( container == null ) {		
				container = hu.emraxxor.web.ide.entities.Container.builder()
									.appdir( appdir )
									.bind( bindport )
									.name( project.getIdentifier() )
									.exposed( Integer.valueOf(command.getExposed() ))
									.status(ContainerStatus.CREATED)
									.image(img)
									.userdir(userdir+"/"+uname).build();
			
				container = containerService.save(container);
				project = projectService.save(project);
				container.setProject(project);
			} 
			
			var containercmd = client.
					 createContainerCmd(img.image())
					.withName(uname+"-"+project.getIdentifier())
					.withAttachStdin(img.attachStdin())
					.withTty(img.tty())
					.withWorkingDir(img.workdir())
					;

			var hostconfig = containercmd.getHostConfig();

			hostconfig
					.withPortBindings(PortBinding.parse(container.getBind() + ":" + command.getExposed()))
					.withBinds(Bind.parse(container.getAppdir() + ":/app"))
					;

			resp = containercmd.exec();

			container.setContainerId(resp.getId());
			containerService.save(container);
			
			return Optional.of(resp);
		}
		return Optional.empty();
	}
	
}
