package com.github.emraxxor.web.ide.service;

import com.github.emraxxor.web.ide.entities.Container;
import com.github.emraxxor.web.ide.entities.User;
import com.github.emraxxor.web.ide.repositories.ContainerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

/**
 * 
 * @author Attila Barna
 *
 */
@Service
@Transactional
@RequiredArgsConstructor
public class ContainerService extends BasicServiceAdapter<Container, Long, ContainerRepository>{

	// TEST CASE
	@Autowired
	private final ContainerRepository repository;

	public Optional<Container> findByBind(int bind) {
		return repository.findByBind(bind);
	}
	
	public Optional<Container> findByContainerIdAndUser(String id, User user) {
		return repository.findByContainerIdAndProject_user(id, user);
	}
	
	public List<Container> findContainers(User user) {
		return repository.findByProject_user(user);
	}
}
