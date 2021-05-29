package com.github.emraxxor.web.ide.service;

import java.util.Optional;

import com.github.emraxxor.web.ide.entities.User;
import com.github.emraxxor.web.ide.entities.UserLog;
import com.github.emraxxor.web.ide.repositories.UserLogRepository;
import com.github.emraxxor.web.ide.repositories.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.emraxxor.web.ide.data.type.UserFormElement;
import lombok.AllArgsConstructor;

import javax.annotation.CheckForNull;

/**
 * 
 * @author Attila Barna
 *
 */
@Service
@Transactional
@AllArgsConstructor
public class UserService {

	private final UserRepository repository;

	private final UserLogRepository userLogRepository;
	
	public Optional<User> findUserByNeptunId(String neptunId) {
		return repository.findByNeptunId(neptunId);
	}
	
	public Optional<User> findUserByEmail(String email) {
		return repository.findByUserMail(email);
	}
	
	public User save(User u) {
		return repository.save(u);
	}

	public UserLog save(UserLog u) {
		return userLogRepository.save(u);
	}
	
	public Optional<User> findById(Long id) {
		return repository.findById(id);
	}
	
	public Optional<User> current() {
		var curr = (UserFormElement)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		return findById(curr.getUserId());
	}

	public UserFormElement principal() {
		return (UserFormElement)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	}

	@CheckForNull
	public User curr() {
		if ( current().isPresent() ) {
			return current().get();
		}
		return null;
	}

}
