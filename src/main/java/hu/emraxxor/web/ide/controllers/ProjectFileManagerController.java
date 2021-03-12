package hu.emraxxor.web.ide.controllers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import hu.emraxxor.web.ide.config.UserProperties;
import hu.emraxxor.web.ide.core.web.BasicSecureFunctions;
import hu.emraxxor.web.ide.data.type.project.ProjectFile;
import hu.emraxxor.web.ide.data.type.project.ProjectFileType;
import hu.emraxxor.web.ide.data.type.response.StatusResponse;
import hu.emraxxor.web.ide.service.ProjectService;
import hu.emraxxor.web.ide.service.UserService;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

@RestController
@RequestMapping("/api/project-filemanager/{projectid}")
@Log4j2
public class ProjectFileManagerController {

	@Autowired
	private ProjectService projectService;

	@Autowired
	private UserService userService;
	
	@Autowired
	private UserProperties userprops;

	@PutMapping
	public ResponseEntity<StatusResponse> put(
			@PathVariable("projectid") Long projectId,
			@Valid @RequestBody ProjectFile file,
			@RequestParam(name = "dir", required = false) Optional<String> dir
		) {
		var user = userService.curr();
		var project = projectService.findByUserAndProjectId(user, projectId);
		if ( project.isPresent() ) {
			var userdir = userprops.getStorage();
			var appdir = new StringBuilder( userdir + "/" + user.getNeptunId() +  "/projects/" + project.get().getIdentifier() );
			var data = Base64.decodeBase64(file.getData());
			
			if ( dir.isPresent() ) 
				appdir.append(String.format("/%s", dir.get()));
			
			appdir.append(String.format("/%s", file.getName() ));
			var ffile = new File(appdir.toString());
			
			try {
				FileUtils.writeByteArrayToFile(ffile , data);
				return ResponseEntity.status(HttpStatus.CREATED).body(StatusResponse.success(ffile));
			} catch (IOException e) {
				log.error(e.getMessage());
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
			}
		}
		return ResponseEntity.notFound().build();
	}
	
	@DeleteMapping("/file/**")
	@SneakyThrows
	public ResponseEntity<StatusResponse> deleteFile(HttpServletRequest request, @PathVariable("projectid") Long projectId
		) {
		var file = request.getRequestURI().split(request.getContextPath() + "/file/")[1];
		var user = userService.curr();
		var project = projectService.findByUserAndProjectId(user, projectId);
		if ( project.isPresent() && !BasicSecureFunctions.directoryTraversalInputCheckStartsWith(file) ) {
			var userdir = userprops.getStorage();
			var appdir = new StringBuilder( userdir + "/" + user.getNeptunId() +  "/projects/" + project.get().getIdentifier() );
			appdir.append(String.format("/%s", file ));
			
			var ffile = new File(appdir.toString());
			
			if ( ffile.exists() ) {
				 ffile.delete();
				 return ResponseEntity.status(HttpStatus.ACCEPTED).build();
			}
		}
		return ResponseEntity.notFound().build();
	}
	
	@GetMapping("/file/**")
	@SneakyThrows
	public ResponseEntity<StatusResponse> getFile(HttpServletRequest request, @PathVariable("projectid") Long projectId
		) {
		var file = request.getRequestURI().split(request.getContextPath() + "/file/")[1];
		var user = userService.curr();
		var project = projectService.findByUserAndProjectId(user, projectId);
		if ( project.isPresent() && !BasicSecureFunctions.directoryTraversalInputCheckStartsWith(file) ) {
			var userdir = userprops.getStorage();
			var appdir = new StringBuilder( userdir + "/" + user.getNeptunId() +  "/projects/" + project.get().getIdentifier() );
			appdir.append(String.format("/%s", file ));
			
			var ffile = new File(appdir.toString());
			
			if ( ffile.exists() ) {
				 return  ResponseEntity.ok( 
						 StatusResponse.success(
								 ProjectFile
								 .builder()
								 .type(ProjectFileType.FILE)
								 .data(  Base64.encodeBase64String( FileUtils.readFileToByteArray(ffile) ) )
								 .name(ffile.getName())
								 .build()
						 	)
						 );
			}
		}
		return ResponseEntity.notFound().build();
	}
	
	
	@GetMapping
	@SneakyThrows
	public ResponseEntity<StatusResponse> filesForDirectory(
					@PathVariable("projectid") Long projectId, 
					@RequestParam(name = "dir", required = false) Optional<String> dir) {
		var user = userService.curr();
		var project = projectService.findByUserAndProjectId(user, projectId);
		if ( project.isPresent() ) {
			var userdir = userprops.getStorage();
			var appdir = new StringBuilder( userdir + "/" + user.getNeptunId() +  "/projects/" + project.get().getIdentifier() );
			var files = Lists.newArrayList();
			
			if ( dir.isPresent() ) 
				appdir.append(String.format("/%s", dir.get()));
			
	        try (Stream<Path> paths = Files.walk(Paths.get(appdir.toString()),1)) {
	            paths
	            	.filter(  e -> !e.getFileName().toString().equals(project.get().getIdentifier()) )
	            	.forEach( e -> { 
	            		 if ( Files.isDirectory(e) ) {
	            			 files.add( ProjectFile.builder().type(ProjectFileType.DIR).name(e.getFileName().toString()).build() );
	            		 } else {
	            			 files.add( ProjectFile.builder().type(ProjectFileType.FILE).name(e.getFileName().toString()).build() );
	            		 }
	            	} );
	        }

			return ResponseEntity.ok(StatusResponse.success(files));
		}
		return ResponseEntity.notFound().build();
	}
	
}
