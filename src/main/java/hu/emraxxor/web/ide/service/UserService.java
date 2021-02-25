package hu.emraxxor.web.ide.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hu.emraxxor.web.ide.data.type.UserFormElement;
import hu.emraxxor.web.ide.entities.User;
import hu.emraxxor.web.ide.repositories.UserRepository;

/**
 * 
 * @author attila
 *
 */
@Service
@Transactional
public class UserService {

	@Autowired
	private UserRepository repository;
	
	public Optional<User> findUserByNeptunId(String neptunId) {
		return repository.findByNeptunId(neptunId);
	}
	
	public Optional<User> findUserByEmail(String email) {
		return repository.findByUserMail(email);
	}
	
	public User save(User u) {
		return repository.save(u);
	}
	
	public Optional<User> findById(Long id) {
		return repository.findById(id);
	}
	
	public Optional<User> current() {
		var curr = (UserFormElement)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		return findById(curr.getUserId());
	}
	
	public User curr() {
		return current().get();
	}

}
