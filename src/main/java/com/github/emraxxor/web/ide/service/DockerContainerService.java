package com.github.emraxxor.web.ide.service;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.nio.charset.Charset;
import java.util.*;
import java.util.function.BiFunction;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import com.github.emraxxor.web.ide.data.type.docker.*;
import org.springframework.stereotype.Service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.PortBinding;

import com.github.emraxxor.web.ide.config.UserProperties;
import com.github.emraxxor.web.ide.data.type.docker.consumer.FrameConsumerResultCallback;
import com.github.emraxxor.web.ide.data.type.docker.consumer.OutputFrame;
import com.github.emraxxor.web.ide.data.type.docker.consumer.ToStringConsumer;
import com.github.emraxxor.web.ide.entities.Project;
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

	private final DockerClient client;
	
	private final UserProperties userprops;
	
	private final UserService userService;
	
	private final ContainerService containerService;
	
	private final ProjectService projectService;


	@PostConstruct
	public void postConstruct() {
		projectService.setDockerContainerService(this);
	}

	/**
	 * List of the created containers 
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
				log.error(e.getMessage(),e);
			} finally {
		        if (ds != null) 
		            ds.close();

		        if (ss != null) {
		            try {
		                ss.close();
		            } catch (IOException e) {
		            	log.error(e.getMessage(),e);
					}
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
	 */
	@Synchronized
	public Optional<Container> containerByProject(Project project) {
		var user = project.getUser();
		for(var c : containers()) {
				if ( Arrays.stream( c.getNames() )
						.anyMatch( e -> e.equals( String.format("/%s-%s", user.getNeptunId(),project.getIdentifier()) ) ) ) {
					return Optional.of(c);
				}
		}
		return Optional.empty();
	}
	
	/**
	 * Stops the given container
	 */
	@Synchronized
	public Boolean stopContainer(DockerContainerElement el) {
		return authorizedCmd(el, ( e,f ) ->  f.stopContainerCmd(e.getContainerId()).exec() ).isEmpty();
	}

	@Synchronized
	public Boolean stopContainer(Project project) {
		if ( project.getContainer() != null ) {
			var container = new DockerContainerElement(project.getContainer().getContainerId());
			var inspectResponse = this.inspect(container);
			if ( !Objects.equals(inspectResponse.getState().getStatus(), "created")) {
				try {
					return authorizedCmd(container, (e, f) -> f.stopContainerCmd(e.getContainerId()).exec()).isEmpty();
				} catch (Exception e) {
					log.info(e.getMessage());
				}
			}
			return true;
		}
		return true;
	}

	@Synchronized
	public void removeContainer(Project project) {
		if ( project.getContainer() != null ) {
			var container = new DockerContainerElement(project.getContainer().getContainerId());
			authorizedCmd(container, (e, f) -> f.removeContainerCmd(e.getContainerId()).exec());
		}
	}
	
	
	/**
	 * Kills the container
	 */
	@Synchronized
	public void killContainer(DockerContainerElement el) {
		client.killContainerCmd(el.getId());
	}
	
	/**
	 * It is suitable for running a command on a given container.
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
	 */
	@Transactional(value = TxType.MANDATORY)
	int randomBind() {
		var port = randomAvailablePort();
		
		while( containerService.findByBind(port).isPresent() )
			port = randomAvailablePort();
		
		return port;
	}

	/**
	 * Output of the docker log command
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
										strBuilder.append(item.toString()).append('\n');
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
	 */
	@Synchronized
	public Optional<String> log(Project project) {
		return log(new DockerContainerElement(project.getContainer().getContainerId() ));
	}
	
	/**
	 * Starts the given container by its id
	 */
	public Boolean start(DockerContainerElement cmd) {
		var inspect =  authorizedCmd( cmd , (e,f) -> f.inspectContainerCmd(e.getContainerId()).exec() );
		if ( inspect.isPresent() ) {
			var inspectResponse = (InspectContainerResponse) inspect.get() ;
			if ( !Objects.equals(inspectResponse.getState().getStatus(), "true")) {
				try {
					return authorizedCmd(cmd, (e, f) -> f.startContainerCmd(e.getContainerId()).exec()).isEmpty();
				} catch(Exception e) {
					log.error(e.getMessage() , e);
				}
			}

		}
		return true;
	}
	
	/**
	 * Starts the container that belongs to the given project
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
	 * Useful for repeatable commands
	 */
	@Synchronized
	private <T extends com.github.emraxxor.web.ide.entities.Container> Optional<?> authorizedCmd(DockerContainerElement el, BiFunction<T,DockerClient,?> fn) {
		var curr = userService.curr();
		var container = containerService.findByContainerIdAndUser(el.getId(), curr);
        if ( container.isPresent() ) 
        	return Optional.ofNullable( fn.apply(  (T) container.get() , client ) );
        return Optional.empty();
	}

	public boolean restartContainer(Project project) {
	    var container = project.getContainer();
	    if ( container != null && container.getContainerId() != null ){
			client.stopContainerCmd(container.getContainerId()).exec();
			client.removeContainerCmd(container.getContainerId()).exec();
			return  true;
		}
	    return  false;
	}

	/**
	 * Inspects the given container
	 */
	public InspectContainerResponse inspect(Project project) {
		return inspect( new DockerContainerElement( String.format("/%s-%s", project.getUser().getNeptunId(),project.getIdentifier())  ) );
	}

	/**
	 * Running containers
	 */
	public List<Container> running() {
		return client
				.listContainersCmd()
				.withShowSize(true)
				.withShowAll(false)
				.exec();
	}
	
	
	/**
	 * Inspects the given container
	 */
	@Synchronized
	public InspectContainerResponse inspect(DockerContainerElement inspectcmd) {
		return client.inspectContainerCmd(inspectcmd.getId()).withContainerId(inspectcmd.getId()).exec();
	}

	/**
	 * Creates a new container with the given image
	 */
	@Synchronized
	@Transactional
	public 	Optional<CreateContainerResponse> update(DockerContainerCommand command) {
		var img = command.getImage();
		var user = userService.curr();
		var cproject = projectService.find(command.getProjectId());

		if ( cproject.isPresent()  && cproject.get().getUser().equals(user) ) {
			var project = cproject.get();
			var container = project.getContainer();
			var uname = user.getNeptunId();
			var userdir = userprops.getStorage();
			var bindport = randomBind();
			var appdir = userdir + "/" + uname +  "/projects/" + project.getIdentifier();

			if ( container != null ) {
				client.stopContainerCmd(container.getContainerId()).exec();
				client.removeContainerCmd(container.getContainerId()).exec();

				container.setAppdir(appdir);
				container.setBind(bindport);
				container.setExposed(command.getExposed());
				container.setStatus(ContainerStatus.CREATED);
				container.setImage(img);
				container.setUserdir(userdir+"/"+uname);

				return getCreateContainerResponse(command, img, project, container, uname);
			}
		}
		return Optional.empty();
	}

	private Optional<CreateContainerResponse> getCreateContainerResponse(DockerContainerCommand command, DockerContainerImage img, Project project, com.github.emraxxor.web.ide.entities.Container container, String uname) {
		CreateContainerResponse resp;
		var containercmd = client.
				 createContainerCmd(img.image())
				.withName(uname+"-"+project.getIdentifier())
				.withAttachStdin(img.attachStdin())
				.withTty(img.tty())
				.withWorkingDir(img.workdir())
				;

		var hostconfig = containercmd.getHostConfig();

		Objects.requireNonNull(hostconfig)
				.withPortBindings(PortBinding.parse(container.getBind() + ":" + command.getExposed()  ))
				.withBinds(Bind.parse(container.getAppdir() + ":/app"))
			;

		resp = containercmd.exec();
		container.setContainerId(resp.getId());
		containerService.save(container);
		return Optional.of(resp);
	}


	/**
	 * Creates a new container with the given image
	 */
	@Synchronized
	@Transactional
	public 	Optional<CreateContainerResponse> create(DockerContainerCommand command) {
		var img = command.getImage();
		var user = userService.curr();
		var cproject = projectService.find(command.getProjectId());
		
		if ( cproject.isPresent()  && cproject.get().getUser().equals(user) ) {
			var project = cproject.get();
			var container = project.getContainer();
			var uname = user.getNeptunId();
			var userdir = userprops.getStorage();
			var bindport = randomBind();
			var appdir = userdir + "/" + uname +  "/projects/" + project.getIdentifier();
				
			if ( container == null ) {		
				container = com.github.emraxxor.web.ide.entities.Container.builder()
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

			return getCreateContainerResponse(command, img, project, container, uname);
		}
		return Optional.empty();
	}
	
}
