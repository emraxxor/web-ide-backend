package hu.emraxxor.web.ide.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import hu.emraxxor.web.ide.entities.Project;
import hu.emraxxor.web.ide.entities.User;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

	
	public Optional<Project> findByIdentifier(String e);
	
	public List<Project> findByUser_userId(Long uid);
	
	public List<Project> findByUser(User user);
}
