package com.github.emraxxor.web.ide.controllers;

import com.github.emraxxor.web.ide.data.type.ProjectFormDeleteElement;
import com.github.emraxxor.web.ide.data.type.ProjectFormElement;
import com.github.emraxxor.web.ide.data.type.response.StatusResponse;
import com.github.emraxxor.web.ide.service.DockerContainerService;
import com.github.emraxxor.web.ide.service.ProjectService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 *
 * @author Attila Barna
 *
 */
@ApiOperation(value = "/api/admin/project", tags = "Project Controller" )
@Api("ProjectController helps to manage projects")
@RestController
@RequestMapping("/api/admin/project")
@AllArgsConstructor

public class AdminProjectController {

    private final ProjectService projectService;

    private final DockerContainerService dockerContainerService;

    @ApiOperation(value = "Removes the given project")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Project is deleted successfully" ),
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('docker:container') and hasRole('ROLE_ADMIN')")
    public ResponseEntity<StatusResponse> deleteByAdmin(@PathVariable Long id) {
        return ResponseEntity.ok( StatusResponse.success( projectService.deleteByAdmin(new ProjectFormDeleteElement(id))  ));
    }


    @GetMapping
    @PreAuthorize("hasAuthority('docker:container') and hasAuthority('docker:admin')")
    public ResponseEntity<StatusResponse> projects() {
        var containers = dockerContainerService.containers();
        return ResponseEntity.ok(StatusResponse
                .success(
                        StreamSupport.stream( projectService.findAll().spliterator() , false )
                                .map(e -> {
                                    var element = new ProjectFormElement(e);
                                    var container =  containers
                                            .stream()
                                            .filter( x ->
                                                    Arrays.stream(x.getNames()).anyMatch(xe -> xe.equals( "/"+ e.getUser().getNeptunId() +"-"+ e.getIdentifier() ) )
                                            )
                                            .findAny();
                                    container.ifPresent(element::setContainer);
                                    return element;
                                })
                                .collect(Collectors.toList())
                ));
    }
}
