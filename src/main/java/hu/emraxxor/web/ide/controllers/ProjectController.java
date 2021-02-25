package hu.emraxxor.web.ide.controllers;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import hu.emraxxor.web.ide.data.type.ProjectFormElement;
import hu.emraxxor.web.ide.data.type.response.StatusResponse;
import hu.emraxxor.web.ide.service.ProjectService;

/**
 * 
 * @author Attila Barna
 *
 */
@RestController
@RequestMapping("/api/project")
public class ProjectController {

	@Autowired
	private ProjectService projectService;

	@PostMapping
	public ResponseEntity<StatusResponse> store(@Valid @RequestBody ProjectFormElement data) {
		return ResponseEntity.ok( StatusResponse.success( new ProjectFormElement( projectService.create(data) ) ));
	}

}
