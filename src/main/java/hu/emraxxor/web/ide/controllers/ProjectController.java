package hu.emraxxor.web.ide.controllers;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import hu.emraxxor.web.ide.data.type.ProjectFormDeleteElement;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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


	@ApiOperation(value = "Removes the given project")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Project is deleted successfully" ),
	})
	@DeleteMapping("/{id}")
	@PreAuthorize("hasAuthority('docker:container')")
	public ResponseEntity<StatusResponse> delete(@PathVariable Long id) {
		return ResponseEntity.ok( StatusResponse.success( projectService.delete(new ProjectFormDeleteElement(id))  ));
	}

	@GetMapping("/inspect/{id}")
	@PreAuthorize("hasAuthority('docker:container')")
	public ResponseEntity<StatusResponse> inspect(@ApiParam(value = "Id of the project", required = true) @PathVariable Long id) {
		var project = projectService.findByUserAndProjectId(userService.curr(), id);
		if ( project.isPresent() ) {
			return ResponseEntity.ok(StatusResponse.success( mapper.map( dockerContainerService.inspect(project.get()) , DockerContainerInspectResponse.class) ) );
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
			return ResponseEntity.ok(StatusResponse.success( dockerContainerService.log(project.get()) ));
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
		}
    	return ResponseEntity.notFound().build();
	}

	@PostMapping("/restart/{id}")
	@PreAuthorize("hasAuthority('docker:container')")
	public ResponseEntity<StatusResponse> restartProjectContainer(
			@ApiParam(value = "Id of the project", required = true) @PathVariable Long id
	) {
		var project = projectService.findByUserAndProjectId(userService.curr(), id);
		if ( project.isPresent() ) {
			if ( dockerContainerService.restartContainer(project.get()) ) {
				return ResponseEntity.ok(
						StatusResponse.success(
								mapper.map(
										dockerContainerService.inspect(project.get()) ,
										DockerContainerInspectResponse.class)
						)

				);
			} else {
				return ResponseEntity.ok(StatusResponse.error("Failed to restart container.!"));
			}
		}
		return ResponseEntity.notFound().build();
	}

	@PutMapping("/rename/{id}")
	public ResponseEntity<StatusResponse> renameProject(
			@ApiParam(value = "Id of the project", required = true) @PathVariable Long id,
			@Valid @RequestBody  ProjectFormElement formElement
	) {
		var project = projectService.findByUserAndProjectId(userService.curr(), id);
		if ( project.isPresent() ) {
			var currentProject = project.get();
			currentProject.setName(formElement.getName());
			currentProject = projectService.save(currentProject);
			return ResponseEntity.ok(StatusResponse.success(new ProjectFormElement(currentProject)));
		}
		return ResponseEntity.notFound().build();
	}

	@PostMapping("/stop/{id}")
	@PreAuthorize("hasAuthority('docker:container')")
	public ResponseEntity<StatusResponse> stopProjectContainer(
			@ApiParam(value = "Id of the project", required = true) @PathVariable Long id
	) {
		var project = projectService.findByUserAndProjectId(userService.curr(), id);
		if ( project.isPresent() ) {
			if ( dockerContainerService.stopContainer(project.get()) ) {
				return ResponseEntity.ok(
						StatusResponse.success(
								mapper.map(
										dockerContainerService.inspect(project.get()) ,
										DockerContainerInspectResponse.class)
						)

				);
			} else {
				return ResponseEntity.ok(StatusResponse.error("Failed to stop container.!"));
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

	@GetMapping("/simple")
	public ResponseEntity<StatusResponse> projectWithoutContainers() {
		var curr = userService.curr();

		return ResponseEntity.ok(StatusResponse
				.success(
						projectService
								.projects(userService.curr())
								.stream()
								.map(e -> new ProjectFormElement(e) )
								.collect(Collectors.toList())
				));
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
