package hu.emraxxor.web.ide.service;

import java.io.File;
import java.util.List;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import hu.emraxxor.web.ide.config.UserProperties;
import hu.emraxxor.web.ide.data.type.ProjectFormElement;
import hu.emraxxor.web.ide.entities.Project;
import hu.emraxxor.web.ide.entities.User;
import hu.emraxxor.web.ide.repositories.ProjectRepository;
import lombok.SneakyThrows;

/**
 * 
 * @author Attila Barna
 *
 */
@Service
@Transactional
public class ProjectService extends BasicServiceAdapter<Project, Long, ProjectRepository> {

	@Autowired
	private UserService userService;
	
	@Autowired
	private UserProperties userprops;
	
	@Transactional(value = TxType.MANDATORY)
	private String identifier() {
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
	
	@SneakyThrows
	public Project create(ProjectFormElement frm) {
		var project = frm.toDataElement(Project.class);
		var user = userService.current().get();
		var userdir = userprops.getStorage();
		project.setIdentifier(identifier());
		project.setUser(user);
		var appdir = new File(userdir + "/" + user.getNeptunId() +  "/projects/" + project.getIdentifier());
		
		if ( !appdir.exists() )
			FileUtils.forceMkdir(appdir);
		
		return save(project);
	}
}
