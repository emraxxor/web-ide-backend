package hu.emraxxor.web.ide.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import hu.emraxxor.web.ide.entities.Project;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

	
	public Optional<Project> findByIdentifier(String e);
}
