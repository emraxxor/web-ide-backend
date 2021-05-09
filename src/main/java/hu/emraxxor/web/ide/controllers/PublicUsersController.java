package hu.emraxxor.web.ide.controllers;

import hu.emraxxor.web.ide.data.type.UserFormElement;
import hu.emraxxor.web.ide.data.type.response.StatusResponse;
import hu.emraxxor.web.ide.entities.User;
import hu.emraxxor.web.ide.service.UserService;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 
 * @author Attila Barna
 *
 */
@RestController
@AllArgsConstructor
@RequestMapping("/users")
public class PublicUsersController {

	private final UserService userService;
	
	private final ModelMapper mapper;
	
	private final PasswordEncoder encoder;
	
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
