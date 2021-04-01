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

import org.springframework.stereotype.Service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.core.command.LogContainerResultCallback;

import hu.emraxxor.web.ide.config.UserProperties;
import hu.emraxxor.web.ide.data.type.docker.ContainerStatus;
import hu.emraxxor.web.ide.data.type.docker.DockerContainerCommand;
import hu.emraxxor.web.ide.data.type.docker.DockerContainerElement;
import hu.emraxxor.web.ide.data.type.docker.DockerContainerExecCommand;
import hu.emraxxor.web.ide.data.type.docker.consumer.FrameConsumerResultCallback;
import hu.emraxxor.web.ide.data.type.docker.consumer.OutputFrame;
import hu.emraxxor.web.ide.data.type.docker.consumer.ToStringConsumer;
import hu.emraxxor.web.ide.entities.Project;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.Synchronized;
import lombok.extern.log4j.Log4j2;

/**
 * This DockerContainerService helps maintain containers. 
 * Each container belongs to a project, a project can have only one container which can be selected and modified as desired.
 * 
 * @author Attila Barna
 *
 */
@Service
@AllArgsConstructor
@Log4j2
public class DockerContainerService {

	private DockerClient client;
	
	private UserProperties userprops;
	
	private UserService userService;
	
	private ContainerService containerService;
	
	private ProjectService projectService;
	
	/**
	 * List of the created containers 
	 * @return
	 */
	@Synchronized
	public 	List<Container> containers() {
		return client
					.listContainersCmd()
					.withShowSize(true)
  				    .withShowAll(true)
					.withStatusFilter(Arrays.asList("exited","restarting", "running", "paused", "created"))
					.exec();
	}
	
	
	/**
	 * Checks whether the port is available on the server or not 
	 * @param port
	 * @return
	 */
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
	
	/**
	 * It generates a random port
	 */
	@Synchronized
	private int randomAvailablePort() {
		int port = new Random().nextInt(65000) + 3500;
		while( !available(port) ) 
			port = new Random().nextInt(65000) + 3500;
		
		return port;
	}

	/**
	 * Gets the container for the given project
	 * @param project
	 * @return
	 */
	@Synchronized
	public Optional<Container> containerByProject(Project project) {
		var user = project.getUser();
		for(var c : containers()) {
				if ( Arrays.asList( c.getNames() ).stream()
						.anyMatch( e -> e.equals( String.format("/%s-%s", user.getNeptunId(),project.getIdentifier()) ) ) ) {
					return Optional.of(c);
				}
		}
		return Optional.empty();
	}
	
	/**
	 * Stops the given container
	 * @param el
	 * @return
	 */
	@Synchronized
	public Boolean stopContainer(DockerContainerElement el) {
		return authorizedCmd(el, ( e,f ) ->  f.stopContainerCmd(e.getContainerId()).exec() ).isEmpty();
	}
	
	
	/**
	 * Kills the container
	 * @param el
	 */
	@Synchronized
	public void killContainer(DockerContainerElement el) {
		client.killContainerCmd(el.getId());
	}
	
	/**
	 * It is suitable for running a command on a given container.
	 * @param exec
	 * @return
	 */
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
	
	/**
	 * Free port for the container
	 * @return
	 */
	@Transactional(value = TxType.MANDATORY)
	private int randomBind() {
		var port = randomAvailablePort();
		
		while( containerService.findByBind(port).isPresent() )
			port = randomAvailablePort();
		
		return port;
	}

	/**
	 * Output of the docker log command
	 * @param cmd
	 * @return
	 */
	@Synchronized
	public Optional<String> log(DockerContainerElement cmd) {
		var res = authorizedCmd(cmd, ( e,f ) -> { 
			var strBuilder = new StringBuilder();
			try {
			
				 f
						 		.logContainerCmd(e.getContainerId())
						 		.withStdErr(true)
						 		.withStdOut(true)
						 		.exec(new ResultCallback.Adapter<Frame>(){
									public void onNext(Frame item) {
										strBuilder.append(item.toString() + '\n');
									}
						 		}).awaitCompletion();

			} catch (InterruptedException e1) {
				log.error(e1);
			}	
			return strBuilder;
		});
		return Optional.ofNullable( res.toString() );
	}
	
	
	/**
	 * Output of the docker log command
	 * @param cmd
	 * @return
	 */
	@Synchronized
	public Optional<String> log(Project project) {
		return log(new DockerContainerElement(project.getContainer().getContainerId() ));
	}
	
	/**
	 * Starts the given container by its id
	 * @param cmd
	 * @return
	 */
	public Boolean start(DockerContainerElement cmd) {
		return authorizedCmd(cmd, ( e,f ) ->  f.startContainerCmd(e.getContainerId()).exec() ).isEmpty();
	}
	
	/**
	 * Starts the container that belongs to the given project
	 * @param cmd
	 * @return
	 */
	public Boolean start(Project project) {
		try {
			return start( new DockerContainerElement( project.getContainer().getContainerId() ));
		} catch(IllegalStateException e) {
			log.error(e);
		}
		return false;
	}
	

	/**
	 * Useful for repetable commands
	 * @param <T>
	 * @param el
	 * @param fn
	 * @return
	 */
	@Synchronized
	private <T extends hu.emraxxor.web.ide.entities.Container> Optional<?> authorizedCmd(DockerContainerElement el, BiFunction<T,DockerClient,?> fn) {
		var curr = userService.curr();
		var container = containerService.findByContainerIdAndUser(el.getId(), curr);
        if ( container.isPresent() ) 
        	return Optional.ofNullable( fn.apply(  (T) container.get() , client ) );
        return Optional.empty();
	}

	
	/**
	 * Inspects the given container
	 * @param inspectcmd
	 * @return
	 */
	public InspectContainerResponse inspect(Project project) {
		return inspect( new DockerContainerElement( String.format("/%s-%s", project.getUser().getNeptunId(),project.getIdentifier())  ) );
	}
	
	
	/**
	 * Inspects the given container
	 * @param inspectcmd
	 * @return
	 */
	@Synchronized
	public InspectContainerResponse inspect(DockerContainerElement inspectcmd) {
		return client.inspectContainerCmd(inspectcmd.getId()).exec();
	}
	
	/**
	 * Creates a new container with the given image
	 * @param command
	 * @return
	 */
	@Synchronized
	@Transactional
	@SneakyThrows
	public 	Optional<CreateContainerResponse> create(DockerContainerCommand command) {
		var img = command.getImage();
		var user = userService.current().get();
		var cproject = projectService.find(command.getProjectId());
		
		if ( cproject.isPresent()  && cproject.get().getUser().equals(user) ) {
			CreateContainerResponse resp;
			var project = cproject.get();
			var container = project.getContainer();
			var uname = user.getNeptunId();
			var userdir = userprops.getStorage();
			var bindport = randomBind();
			var appdir = userdir + "/" + uname +  "/projects/" + project.getIdentifier();
				
			if ( container == null ) {		
				container = hu.emraxxor.web.ide.entities.Container.builder()
									.appdir( appdir )
									.bind( bindport )
									.name( project.getIdentifier() )
									.exposed( command.getExposed() )
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
