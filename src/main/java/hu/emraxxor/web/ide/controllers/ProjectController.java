package hu.emraxxor.web.ide.controllers;

import java.util.Arrays;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import hu.emraxxor.web.ide.data.type.ProjectFormElement;
import hu.emraxxor.web.ide.data.type.docker.DockerContainerElement;
import hu.emraxxor.web.ide.data.type.docker.DockerContainerInspectResponse;
import hu.emraxxor.web.ide.data.type.response.StatusResponse;
import hu.emraxxor.web.ide.service.DockerContainerService;
import hu.emraxxor.web.ide.service.ProjectService;
import hu.emraxxor.web.ide.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

/**
 * 
 * @author Attila Barna
 *
 */
@ApiOperation(value = "/api/project", tags = "Project Controller" )
@Api("ProjectController helps to manage projects")
@RestController
@RequestMapping("/api/project")
public class ProjectController {

	@Autowired
	private ProjectService projectService;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private DockerContainerService dockerContainerService;
	
	@Autowired
	private ModelMapper mapper;

	@ApiOperation(value = "Creates a new project")
	@ApiResponses(value = { 
			@ApiResponse(responseCode = "200", description = "Project is created successfully" ),
	})
	@PostMapping
	public ResponseEntity<StatusResponse> store(@Valid @RequestBody ProjectFormElement data) {
		return ResponseEntity.ok( StatusResponse.success( new ProjectFormElement( projectService.create(data) ) ));
	}
	
	@GetMapping("/inspect/{id}")
	@PreAuthorize("hasAuthority('docker:container')")
	public ResponseEntity<StatusResponse> inspect(@ApiParam(value = "Id of the project", required = true) @PathVariable Long id) {
		var project = projectService.findByUserAndProjectId(userService.curr(), id);
		if ( project.isPresent() ) {
			var container = dockerContainerService.containerByProject(project.get());
			if ( container.isPresent() ) {
				return ResponseEntity.ok(StatusResponse.success( mapper.map( dockerContainerService.inspect(project.get()) , DockerContainerInspectResponse.class) ) );
			}
		} 
    	return ResponseEntity.notFound().build();
	}
	
	@GetMapping("/container/{id}")
	@PreAuthorize("hasAuthority('docker:container')")
	public ResponseEntity<StatusResponse> container(
				@ApiParam(value = "Id of the project", required = true) @PathVariable Long id
		) {
		var project = projectService.findByUserAndProjectId(userService.curr(), id);
		if ( project.isPresent() ) {
			var container = dockerContainerService.containerByProject(project.get());
			if ( container.isPresent() ) {
				return ResponseEntity.ok(StatusResponse.success( container.get() ));
			}
		} 
    	return ResponseEntity.notFound().build();
	}
	
	@GetMapping("/log/{id}")
	@PreAuthorize("hasAuthority('docker:container')")
	public ResponseEntity<StatusResponse> log(
				@ApiParam(value = "Id of the project", required = true) @PathVariable Long id
		) {
		var project = projectService.findByUserAndProjectId(userService.curr(), id);
		if ( project.isPresent() ) {
			var container = dockerContainerService.containerByProject(project.get());
			if ( container.isPresent() ) {
				return ResponseEntity.ok(StatusResponse.success( dockerContainerService.log(project.get()) ));
			}
		} 
    	return ResponseEntity.notFound().build();
	}

	@PostMapping("/start/{id}")
	@PreAuthorize("hasAuthority('docker:container')")
	public ResponseEntity<StatusResponse> startProjectContainer(
				@ApiParam(value = "Id of the project", required = true) @PathVariable Long id
		) {
		var project = projectService.findByUserAndProjectId(userService.curr(), id);
		if ( project.isPresent() ) {
			var container = dockerContainerService.containerByProject(project.get());
			if ( container.isPresent() ) {
				if ( dockerContainerService.start(project.get()) ) {
					return ResponseEntity.ok(
							StatusResponse.success(
									mapper.map( 
											dockerContainerService.inspect(project.get()) ,
											DockerContainerInspectResponse.class) 
							)
									 
					);	
				} else {
					return ResponseEntity.ok(StatusResponse.error("Failed to start container.!"));
				}
			} else {
				return ResponseEntity.ok(StatusResponse.error("A project can only be started with an existing container!"));
			}
		} 
    	return ResponseEntity.notFound().build();
	}
	
	@GetMapping("/{id}")
	public ResponseEntity<StatusResponse> projectById(@PathVariable Long id) {
		var user = userService.curr();
		var project = projectService.findByUserAndProjectId(user, id);
		
		if ( project.isPresent() ) 
			return ResponseEntity.ok(StatusResponse.success(new ProjectFormElement(project.get())));
		
		return ResponseEntity.notFound().build();
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
