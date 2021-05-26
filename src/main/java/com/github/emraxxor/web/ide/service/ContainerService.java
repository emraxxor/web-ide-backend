package com.github.emraxxor.web.ide.service;

import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import com.github.emraxxor.web.ide.repositories.ContainerRepository;
import org.springframework.stereotype.Service;

import com.github.emraxxor.web.ide.entities.Container;
import com.github.emraxxor.web.ide.entities.User;

/**
 * 
 * @author Attila Barna
 *
 */
@Service
@Transactional
public class ContainerService extends BasicServiceAdapter<Container, Long, ContainerRepository>{

	
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
