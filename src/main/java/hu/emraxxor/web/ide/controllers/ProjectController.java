package hu.emraxxor.web.ide.controllers;

import hu.emraxxor.web.ide.data.type.ProjectFormDeleteElement;
import hu.emraxxor.web.ide.data.type.ProjectFormElement;
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
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 
 * @author Attila Barna
 *
 */
@ApiOperation(value = "/api/project", tags = "Project Controller" )
@Api("ProjectController helps to manage projects")
@RestController
@RequestMapping("/api/project")
@AllArgsConstructor
public class ProjectController {

	private final ProjectService projectService;
	
	private final UserService userService;
	
	private final DockerContainerService dockerContainerService;
	
	private final ModelMapper mapper;

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

	@ApiOperation(value = "Removes the given project")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Project is deleted successfully" ),
	})
	@DeleteMapping("/admin/{id}")
	@PreAuthorize("hasAuthority('docker:container') and hasRole('ROLE_ADMIN')")
	public ResponseEntity<StatusResponse> deleteByAdmin(@PathVariable Long id) {
		return ResponseEntity.ok( StatusResponse.success( projectService.deleteByAdmin(new ProjectFormDeleteElement(id))  ));
	}


	@GetMapping("/inspect/{id}")
	@PreAuthorize("hasAuthority('docker:container')")
	public ResponseEntity<StatusResponse> inspect(@ApiParam(value = "Id of the project", required = true) @PathVariable Long id) {
		var project = projectService.findByUserAndProjectId(userService.curr(), id);
		return project.map(value -> ResponseEntity.ok(StatusResponse.success(mapper.map(dockerContainerService.inspect(value), DockerContainerInspectResponse.class)))).orElseGet(() -> ResponseEntity.notFound().build());
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
		return project.map(value -> ResponseEntity.ok(StatusResponse.success(dockerContainerService.log(value)))).orElseGet(() -> ResponseEntity.notFound().build());
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
		return project.map(value -> ResponseEntity.ok(StatusResponse.success(new ProjectFormElement(value)))).orElseGet(() -> ResponseEntity.notFound().build());
	}

	@GetMapping("/simple")
	public ResponseEntity<StatusResponse> projectWithoutContainers() {
		return ResponseEntity.ok(StatusResponse
				.success(
						projectService
								.projects(Objects.requireNonNull(userService.curr()))
								.stream()
								.map(ProjectFormElement::new)
								.collect(Collectors.toList())
				));
	}

	@GetMapping
	@PreAuthorize("hasAuthority('docker:container') and hasAuthority('docker:admin')")
	public ResponseEntity<StatusResponse> projects() {
		var containers = dockerContainerService.containers();
		var curr = userService.curr();
		
		return ResponseEntity.ok(StatusResponse
									.success(  
												projectService
													.projects(Objects.requireNonNull(userService.curr()))
													.stream()
													.map(e -> { 
														var element = new ProjectFormElement(e);
														
														var container =  containers
																.stream()
																.filter( x ->  
																	Arrays.stream(x.getNames())
																	.anyMatch(xe -> xe.equals( "/"+ Objects.requireNonNull(curr).getNeptunId()+"-"+ e.getIdentifier() ) )
																 )
																.findAny();

														container.ifPresent(element::setContainer);
														
														return element;
													})
													.collect(Collectors.toList())
									));
	}
}
