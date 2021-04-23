package hu.emraxxor.web.ide.service;

import hu.emraxxor.web.ide.config.UserProperties;
import hu.emraxxor.web.ide.data.type.ProjectFormDeleteElement;
import hu.emraxxor.web.ide.data.type.ProjectFormElement;
import hu.emraxxor.web.ide.entities.Project;
import hu.emraxxor.web.ide.entities.User;
import hu.emraxxor.web.ide.repositories.ProjectRepository;
import lombok.*;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 
 * @author Attila Barna
 *
 */
@Service
@Transactional
@RequiredArgsConstructor
@Log4j2
public class ProjectService extends BasicServiceAdapter<Project, Long, ProjectRepository> {

	@NonNull
	private final UserService userService;

	@NonNull
	private final UserProperties userprops;

	@Setter
	private DockerContainerService dockerContainerService;

	@Transactional(value = TxType.MANDATORY)
	String identifier() {
		var identifier = RandomStringUtils.random(30, true, true);
		while ( repository.findByIdentifier(identifier).isPresent() ) 
			identifier = RandomStringUtils.random(30, true, true);
		
		return identifier;
	}

	public List<Project> projects(Long uid) {
		return repository.findByUser_userId(uid); 
	}
	
	public List<Project> projects(User user) {
		return this.projects(user.getUserId());
	}
	
	public Optional<Project> findByUserAndProjectId(User user, Long id) {
		return repository.findByUserAndId(user, id);
	}
	
	@SneakyThrows
	public Project create(ProjectFormElement frm) {
		var project = frm.toDataElement(Project.class);
		var user = userService.curr();
		var userdir = userprops.getStorage();
		project.setIdentifier(identifier());
		project.setUser(user);
		var appdir = new File(userdir + "/" + Objects.requireNonNull(user).getNeptunId() +  "/projects/" + project.getIdentifier());
		
		if ( !appdir.exists() )
			FileUtils.forceMkdir(appdir);
		
		return save(project);
	}

	public boolean deleteByAdmin(ProjectFormDeleteElement frm) {
		var optionalProject = repository.findById(frm.getId());
		if ( optionalProject.isPresent() ) {
			var project = optionalProject.get();
			var user = project.getUser();
			return deleteUserProject(project, user);
		}
		return false;
	}

	private boolean deleteUserProject(Project project, User user) {
		var userdir = userprops.getStorage();
		dockerContainerService.stopContainer(project);
		dockerContainerService.removeContainer(project);

		var appdir = new File(userdir + "/" + Objects.requireNonNull(user).getNeptunId() + "/projects/" + project.getIdentifier());
		if (appdir.exists()) {
			try {
				FileUtils.deleteDirectory(appdir);
			} catch (IOException e) {
				log.error(e.getMessage(), e);
				return  false;
			}
		}

		repository.delete(project);
		return true;
	}


	public boolean delete(ProjectFormDeleteElement frm) {
		var optionalProject = repository.findById(frm.getId());
		if ( optionalProject.isPresent() ) {
			var project = optionalProject.get();
			var user = userService.curr();
			return deleteUserProject(project, user);
		}
		return false;
	}
}
