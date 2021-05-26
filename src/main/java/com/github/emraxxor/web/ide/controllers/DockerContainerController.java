package com.github.emraxxor.web.ide.controllers;

import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.Container;
import com.github.emraxxor.web.ide.data.type.docker.DockerContainerCommand;
import com.github.emraxxor.web.ide.data.type.docker.DockerContainerExecCommand;
import com.github.emraxxor.web.ide.service.DockerContainerService;
import com.github.emraxxor.web.ide.data.type.ContainerFormElement;
import com.github.emraxxor.web.ide.data.type.docker.DockerContainerElement;
import com.github.emraxxor.web.ide.data.type.docker.DockerContainerInspectResponse;
import com.github.emraxxor.web.ide.data.type.response.StatusResponse;
import com.github.emraxxor.web.ide.service.ContainerService;
import com.github.emraxxor.web.ide.service.UserService;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 
 * @author attila
 *
 */
@RestController
@RequestMapping("/api/docker/container")
@AllArgsConstructor
public class DockerContainerController {

	private final DockerContainerService dockerContainerService;
	
	private final UserService userService;
	
	private final ContainerService containerService;
	
	private final ModelMapper mapper;

	@ApiOperation(value = "List containers")
	@GetMapping
	@PreAuthorize("hasAuthority('docker:container') and hasAuthority('docker:admin')")
	public ResponseEntity<List<Container>> list() {
		return ResponseEntity.ok( dockerContainerService.containers() );
	}

	@ApiOperation(value = "List running containers")
	@GetMapping("/running")
	@PreAuthorize("hasAuthority('docker:container') and hasAuthority('docker:admin')")
	public ResponseEntity<List<Container>> listRunningContainers() {
		return ResponseEntity.ok( dockerContainerService.running() );
	}

	@ApiOperation(value = "Updates the given container")
	@PutMapping
	@PreAuthorize("hasAuthority('docker:container')")
	public ResponseEntity<CreateContainerResponse> update(@Valid @RequestBody DockerContainerCommand cmd) {
		var res = dockerContainerService.update(cmd);
		return res.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
	}

	@ApiOperation(value = "Creates a new container")
	@PostMapping
	@PreAuthorize("hasAuthority('docker:container')")
	public ResponseEntity<CreateContainerResponse> create(@Valid @RequestBody DockerContainerCommand cmd) {
		var res = dockerContainerService.create(cmd);
		return res.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
	}

	@ApiOperation(value = "List containers that are belong to the current user")
	@GetMapping("/created")
	public ResponseEntity<List<ContainerFormElement>> listContainers() {
		return ResponseEntity
						.ok( 
								containerService.findContainers( userService.curr() )
								.stream()
								.map(ContainerFormElement::new)
								.collect(Collectors.toList()) 
		);
	}

	@ApiOperation(value = "Get the log of the containers")
	@GetMapping("/log")
	public ResponseEntity<List<ContainerFormElement>> log() {
		return ResponseEntity
						.ok( 
								containerService.findContainers( userService.curr() )
								.stream()
								.map(ContainerFormElement::new)
								.collect(Collectors.toList()) 
		);
	}

	@ApiOperation(value = "Starts the given container")
	@PostMapping("/start")
	@PreAuthorize("hasAuthority('docker:container')")
	public ResponseEntity<StatusResponse> start(@Valid @RequestBody DockerContainerElement cmd) {
		return ResponseEntity.ok( StatusResponse.success( dockerContainerService.start(cmd) ) );
	}

	@ApiOperation(value = "Stops the given container")
	@PostMapping("/stop")
	@PreAuthorize("hasAuthority('docker:container')")
	public ResponseEntity<StatusResponse> stop(@Valid @RequestBody DockerContainerElement el) {
		return ResponseEntity.ok( StatusResponse.success( dockerContainerService.stopContainer(el) ) );
	}

	@ApiOperation(value = "Force stops the given container")
	@PostMapping("/kill")
	@PreAuthorize("hasAuthority('docker:admin')")
	public ResponseEntity<StatusResponse> kill(@Valid @RequestBody DockerContainerElement el) {
		dockerContainerService.killContainer(el);
		return ResponseEntity.ok().build();
	}

	@ApiOperation(value = "Exec a command in the container")
	@PostMapping("/exec")
	@PreAuthorize("hasAuthority('docker:container')")
	public ResponseEntity<StatusResponse> exec(@Valid @RequestBody DockerContainerExecCommand cmd) {
		Optional<Object> res = dockerContainerService.exec(cmd);
		return res.map(o -> ResponseEntity.ok(StatusResponse.success(o))).orElseGet(() -> ResponseEntity.notFound().build());
	}

	@ApiOperation(value = "Inspects the given container")
	@GetMapping("/inspect")
	@PreAuthorize("hasAuthority('docker:container')")
	public ResponseEntity<StatusResponse> inspect(@Valid DockerContainerElement inspect) {
		return ResponseEntity.ok( StatusResponse.success( mapper.map( dockerContainerService.inspect(inspect) , DockerContainerInspectResponse.class) ) );
	}
}
