package hu.emraxxor.web.ide.service;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.command.StartContainerCmd;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.PortBinding;

import hu.emraxxor.web.ide.config.UserProperties;
import hu.emraxxor.web.ide.data.type.docker.ContainerStatus;
import hu.emraxxor.web.ide.data.type.docker.DockerContainerCommand;
import hu.emraxxor.web.ide.data.type.docker.DockerContainerElement;
import hu.emraxxor.web.ide.data.type.docker.DockerContainerImage;
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
		return client.listContainersCmd().exec();
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
	
	
	@Transactional(value = TxType.MANDATORY)
	private int randomBind() {
		var port = randomAvailablePort();
		
		while( containerService.findByBind(port).isPresent() )
			port = randomAvailablePort();
		
		return port;
	}
	
	@Synchronized
	public Boolean start(DockerContainerElement cmd) {
		var curr = userService.curr();
		var container = containerService.findByContainerIdAndUser(cmd.getId(), curr);
		if ( container.isPresent() ) {
			client.startContainerCmd(cmd.getId()).exec();
			return true;
		}
		return false;
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
			hu.emraxxor.web.ide.entities.Container container;
			var project = cproject.get();
			var img = dockerImg.get();
			var uname = user.getNeptunId();
				
			if ( project.getContainer() == null ) {
				var userdir = userprops.getStorage();
				var appdir = userdir + "/" + uname +  "/projects/" + project.getIdentifier();
				var bindport = randomBind();
				
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
				
				var containercmd = client.createContainerCmd(img.image()).withName(uname);
				var hostconfig = containercmd.getHostConfig();
				
				hostconfig.withPortBindings(PortBinding.parse(bindport + ":" + command.getExposed()))
						  .withBinds(Bind.parse(appdir + ":/app"));
				
				resp = containercmd.exec();
			} else {
				container = project.getContainer();
				var containercmd = client.createContainerCmd(img.image()).withName(uname);
				var hostconfig = containercmd.getHostConfig();
				
				hostconfig.withPortBindings(PortBinding.parse(container.getBind() + ":" + command.getExposed()))
						  .withBinds(Bind.parse(container.getAppdir() + ":/app"));
				
				resp = containercmd.exec();
			}
			container.setContainerId(resp.getId());
			containerService.save(container);
			
			return Optional.of(resp);
		}
		return Optional.empty();
	}
	
}
