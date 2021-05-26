package com.github.emraxxor.web.ide.repositories;

import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.github.emraxxor.web.ide.entities.Container;
import com.github.emraxxor.web.ide.entities.User;

@Repository
public interface ContainerRepository extends JpaRepository<Container, Long> {
	
	Optional<Container> findByBind(int bind);
	
	Optional<Container> findByContainerIdAndProject_user(String id, User user);

	List<Container> findByProject_user(User user);
		
}
