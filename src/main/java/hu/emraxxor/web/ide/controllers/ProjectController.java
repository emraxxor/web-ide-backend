package hu.emraxxor.web.ide.controllers;

import java.util.Arrays;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import hu.emraxxor.web.ide.data.type.ProjectFormElement;
import hu.emraxxor.web.ide.data.type.response.StatusResponse;
import hu.emraxxor.web.ide.service.DockerContainerService;
import hu.emraxxor.web.ide.service.ProjectService;
import hu.emraxxor.web.ide.service.UserService;

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
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private DockerContainerService dockerContainerService;

	@PostMapping
	public ResponseEntity<StatusResponse> store(@Valid @RequestBody ProjectFormElement data) {
		return ResponseEntity.ok( StatusResponse.success( new ProjectFormElement( projectService.create(data) ) ));
	}

	@GetMapping
	public ResponseEntity<StatusResponse> projects() {
		var containers = dockerContainerService.containers();
		var curr = userService.curr();
		
		return ResponseEntity.ok(StatusResponse
									.success(  
												projectService
													.projects(userService.curr())
													.stream()
													.map(e -> { 
														var element = new ProjectFormElement(e);
														
														var container =  containers
																.stream()
																.filter( x ->  
																	Arrays.asList(x.getNames())
																	.stream()
																	.anyMatch(xe -> xe.equals( "/"+curr.getNeptunId()+"-"+ e.getIdentifier() ) )
																 )
																.findAny();
														
														if ( container.isPresent() ) {
															element.setContainer(container.get());
														}
														
														return element;
													})
													.collect(Collectors.toList())
									));
	}
}
