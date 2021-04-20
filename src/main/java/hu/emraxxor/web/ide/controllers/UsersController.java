package hu.emraxxor.web.ide.controllers;

import java.io.IOException;

import hu.emraxxor.web.ide.data.type.*;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import hu.emraxxor.web.ide.data.type.response.StatusResponse;
import hu.emraxxor.web.ide.service.ProfileStorageService;
import hu.emraxxor.web.ide.service.UserService;

import javax.validation.Valid;


/**
 * 
 * @author Attila Barna
 *
 */
@RestController
@RequestMapping("/api/user")
public class UsersController {

	@Autowired
	private UserService userService;
	
	@Autowired
	private ProfileStorageService profileStorage;
	
	@Autowired
	private ModelMapper mapper;

	@Autowired
	private PasswordEncoder encoder;


	@GetMapping("/info")
	public UserFormElement info() {
		var curr = (UserFormElement)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		return FormElement.convertTo( userService.findById( curr.getUserId() ).get(), UserFormElement.class );
	}
	
	@PutMapping("/image")
	public ResponseEntity<StatusResponse> image(@RequestBody ImageData data) {
		if ( data.getData() != null ) {
			var curr = userService.current().get();
			profileStorage.remove(curr.getImage());
			FileInfo finfo = profileStorage.storeFile(data.getData());
			curr.setImage(finfo.name());
			userService.save(curr);
			return ResponseEntity.ok(StatusResponse.success());
		}
		return ResponseEntity.notFound().build();
	}
	
	@GetMapping("/image")
	public ResponseEntity<StatusResponse> image() {
		try {
			var curr = userService.current().get();
			return ResponseEntity.ok(StatusResponse.success(profileStorage.file(curr.getImage())));
		} catch (IOException e) {
			 return ResponseEntity.notFound().build();
		}
	}


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
	
	@PutMapping("/personal")
	public ResponseEntity<StatusResponse> update(@RequestBody UserProfilePersonalFormElement data) {
		UserFormElement curr = (UserFormElement)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		var u = userService.findById( curr.getUserId() );
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
