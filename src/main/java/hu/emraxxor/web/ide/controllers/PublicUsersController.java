package hu.emraxxor.web.ide.controllers;

import javax.validation.Valid;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import hu.emraxxor.web.ide.data.type.UserFormElement;
import hu.emraxxor.web.ide.data.type.response.StatusResponse;
import hu.emraxxor.web.ide.entities.User;
import hu.emraxxor.web.ide.service.UserService;

/**
 * 
 * @author Attila Barna
 *
 */
@RestController
@RequestMapping("/users")
public class PublicUsersController {

	@Autowired
	private UserService userService;
	
	@Autowired
	private ModelMapper mapper;
	
	@Autowired
	private PasswordEncoder encoder;
	
	@PostMapping
	public ResponseEntity<?> registration(@Valid @RequestBody UserFormElement user) {
		if ( userService.findUserByEmail(user.getUserMail()).isEmpty() && userService.findUserByNeptunId(user.getNeptunId()).isEmpty()) {
			User u = mapper.map(user, User.class);
			u.setUserPassword(encoder.encode(user.getUserPassword()));
			userService.save(u);
			return ResponseEntity.status(HttpStatus.CREATED).body(StatusResponse.success());
		} 
		return ResponseEntity.badRequest().body(StatusResponse.error("UniqueConstraintException"));
	}
	
    @RequestMapping( value="/{username}", method = RequestMethod.HEAD)
    public ResponseEntity<?> exists(@PathVariable String username) {
    	if ( userService.findUserByNeptunId(username).isPresent() ) 
    		return ResponseEntity.ok().build();
    	
    	return ResponseEntity.notFound().build();
    }

	
}
