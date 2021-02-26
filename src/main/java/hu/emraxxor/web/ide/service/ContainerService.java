package hu.emraxxor.web.ide.service;

import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import hu.emraxxor.web.ide.entities.Container;
import hu.emraxxor.web.ide.entities.User;
import hu.emraxxor.web.ide.repositories.ContainerRepository;

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
