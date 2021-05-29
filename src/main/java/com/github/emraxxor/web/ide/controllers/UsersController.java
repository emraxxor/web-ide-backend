package com.github.emraxxor.web.ide.controllers;

import com.github.emraxxor.web.ide.data.type.*;
import com.github.emraxxor.web.ide.data.type.response.StatusResponse;
import com.github.emraxxor.web.ide.service.ProfileStorageService;
import com.github.emraxxor.web.ide.service.UserService;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;


/**
 * 
 * @author Attila Barna
 *
 */
@ApiOperation(value = "/api/user", tags = "Users Controller")
@RestController
@RequestMapping("/api/user")
public class UsersController {

	private final UserService userService;
	
	private final ProfileStorageService profileStorage;
	
	private final PasswordEncoder encoder;

	public UsersController(UserService userService, ProfileStorageService profileStorage, PasswordEncoder encoder) {
		this.userService = userService;
		this.profileStorage = profileStorage;
		this.encoder = encoder;
	}

	@ApiOperation(value = "Information about the current user")
	@GetMapping("/info")
	public UserFormElement info() {
		return FormElement.convertTo( userService.curr() , UserFormElement.class );
	}

	@ApiOperation(value = "Upload an image for the current user")
	@PutMapping("/image")
	public ResponseEntity<StatusResponse> image(@RequestBody ImageData data) {
		if ( data.getData() != null && userService.current().isPresent() ) {
			var curr = userService.current().get();
			profileStorage.remove(curr.getImage());
			FileInfo finfo = profileStorage.storeFile(data.getData());
			curr.setImage(finfo.name());
			userService.save(curr);
			return ResponseEntity.ok(StatusResponse.success());
		}
		return ResponseEntity.notFound().build();
	}

	@ApiOperation(value = "Image of the current user")
	@GetMapping("/image")
	public ResponseEntity<StatusResponse> image() {
		try {
			if ( userService.current().isPresent() ) {
				var curr = userService.current().get();
				return ResponseEntity.ok(StatusResponse.success(profileStorage.file(curr.getImage())));
			}
		 	return ResponseEntity.notFound().build();
		} catch (IOException e) {
			return ResponseEntity.notFound().build();
		}
	}

	@ApiOperation(value = "Update user profile information")
	@PutMapping("/profile")
	public ResponseEntity<StatusResponse> update(@Valid  @RequestBody UserProfileFormElement data) {
		UserFormElement curr = (UserFormElement)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		var u = userService.findById( curr.getUserId() );
		if ( u.isPresent() ) {
			var persistent = u.get();
			FormElement.updateFrom( data , persistent);
			return ResponseEntity.ok(  StatusResponse.success( FormElement.convertTo( userService.save(persistent)  , UserFormElement.class ) ));
		}
		return ResponseEntity.notFound().build();
	}

	@ApiOperation(value = "Update user personal information")
	@PutMapping("/personal")
	public ResponseEntity<StatusResponse> update(@RequestBody UserProfilePersonalFormElement data) {
		var curr = userService.principal();
		var u = userService.current();
		if ( u.isPresent() ) {
			var persistent = u.get();
			if ( data.getUserMail() != null && data.getOldUserPassword() != null && encoder.matches(data.getOldUserPassword(), persistent.getUserPassword()) ) {
				if ( !persistent.getUserMail().equals(curr.getUserMail()) && userService.findUserByEmail(data.getUserMail()).isPresent() )
					return ResponseEntity.badRequest().body(StatusResponse.error(data));

				if ( data.getUserPassword() != null && data.getUserPassword().length() > 3 && !data.getUserPassword().equals(data.getConfirmUserPassword()) )
					return ResponseEntity.badRequest().body(StatusResponse.error(data));

				if ( data.getUserPassword() != null && !data.getUserPassword().equals("") && data.getUserPassword().length() < 5)
					return ResponseEntity.badRequest().body(StatusResponse.error(data));

				if ( !data.getUserMail().equals(curr.getUserMail())  )
					persistent.setUserMail(data.getUserMail());

				if ( data.getUserPassword() != null && data.getUserPassword().length() > 5 )
					persistent.setUserPassword(encoder.encode(data.getUserPassword()));

				return ResponseEntity.ok(  StatusResponse.success( FormElement.convertTo( userService.save(persistent)  , UserFormElement.class ) ));
			}
			return ResponseEntity.ok(  StatusResponse.error(data) );
		}
		return ResponseEntity.notFound().build();
	}
}
