package com.github.emraxxor.web.ide.controllers;

import com.github.emraxxor.web.ide.data.type.UserFormElement;
import com.github.emraxxor.web.ide.data.type.response.StatusResponse;
import com.github.emraxxor.web.ide.entities.User;
import com.github.emraxxor.web.ide.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
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
@Api(value = "Users controller")
@RestController
@AllArgsConstructor
@RequestMapping("/users")
public class PublicUsersController {

	private final UserService userService;
	
	private final ModelMapper mapper;
	
	private final PasswordEncoder encoder;

	@ApiOperation(value = "Creates a new user")
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

	@ApiOperation(value = "Checks the existence of the given user")
	@RequestMapping( value="/{username}", method = RequestMethod.HEAD)
    public ResponseEntity<?> exists(@PathVariable String username) {
    	if ( userService.findUserByNeptunId(username).isPresent() ) 
    		return ResponseEntity.ok().build();
    	
    	return ResponseEntity.notFound().build();
    }

	
}
