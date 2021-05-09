package hu.emraxxor.web.ide.controllers;

import hu.emraxxor.web.ide.config.UserProperties;
import hu.emraxxor.web.ide.core.web.BasicSecureFunctions;
import hu.emraxxor.web.ide.data.type.project.ProjectFile;
import hu.emraxxor.web.ide.data.type.project.ProjectFileType;
import hu.emraxxor.web.ide.data.type.response.StatusResponse;
import hu.emraxxor.web.ide.service.ProjectService;
import hu.emraxxor.web.ide.service.UserService;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.io.FileUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static hu.emraxxor.web.ide.core.web.BasicSecureFunctions.decode;

@RestController
@RequestMapping("/api/project-filemanager/{projectid}")
@Log4j2
public class ProjectFileManagerController {

	private final ProjectService projectService;

	private final UserService userService;
	
	private final UserProperties userprops;

	public ProjectFileManagerController(ProjectService projectService, UserService userService, UserProperties userprops) {
		this.projectService = projectService;
		this.userService = userService;
		this.userprops = userprops;
	}

	@PutMapping
	@SneakyThrows
	public ResponseEntity<StatusResponse> put(
			@PathVariable("projectid") Long projectId,
			@Valid @RequestBody ProjectFile file,
			@RequestParam(name = "dir", required = false) Optional<String> dir
		) {
		var user = userService.curr();
		var project = projectService.findByUserAndProjectId(user, projectId);
		if ( project.isPresent() ) {
			var userdir = userprops.getStorage();
			var appdir = new StringBuilder( userdir + "/" + Objects.requireNonNull(user).getNeptunId() +  "/projects/" + project.get().getIdentifier() );
			var data = Base64.decodeBase64(file.getData());
			
			if ( dir.isPresent() ) 
				appdir.append(String.format("/%s", decode( dir.get() ) ) );
			
			appdir.append(String.format("/%s",  decode( file.getName() ) ));
			var cleaned = appdir.toString().replace("//","/");
			var ffile = new File( cleaned );
			
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
			var cleaned = (userdir + "/" + Objects.requireNonNull(user).getNeptunId() + "/projects/" + project.get().getIdentifier() + String.format("/%s", decode(file))).replace("//","/");
			var ffile = new File(cleaned);

			if ( ffile.exists() ) {
				 ffile.delete();
				 return ResponseEntity.status(HttpStatus.ACCEPTED).build();
			}
		}
		return ResponseEntity.notFound().build();
	}
	
	@PutMapping("/file/**")
	@SneakyThrows
	public ResponseEntity<StatusResponse> updateFile(HttpServletRequest request, @PathVariable("projectid") Long projectId,
		@Valid @RequestBody Map<String, Object> data
		) {
		var file = request.getRequestURI().split(request.getContextPath() + "/file/")[1];
		var user = userService.curr();
		var project = projectService.findByUserAndProjectId(user, projectId);
		if ( project.isPresent() && !BasicSecureFunctions.directoryTraversalInputCheckStartsWith(file) ) {
			var userdir = userprops.getStorage();
			var cleaned = (userdir + "/" + Objects.requireNonNull(user).getNeptunId() + "/projects/" + project.get().getIdentifier() + String.format("/%s", decode(file))).replace("//","/");
			var ffile = new File(cleaned);
			
			FileUtils.writeByteArrayToFile(ffile , ((String)data.get("data")).getBytes() );
			return ResponseEntity.status(HttpStatus.ACCEPTED).build();
		}
		return ResponseEntity.notFound().build();
	}
	
	
	@PutMapping("/rename/file/**")
	@SneakyThrows
	public ResponseEntity<StatusResponse> renameFile(
			HttpServletRequest request,
			@PathVariable("projectid") Long projectId,
			@Valid @RequestBody ProjectFile projectFile
		) {
		var file = request.getRequestURI().split(request.getContextPath() + "/file/")[1];
		var user = userService.curr();
		var project = projectService.findByUserAndProjectId(user, projectId);
		if ( project.isPresent() && !BasicSecureFunctions.directoryTraversalInputCheckStartsWith(file) ) {
			var userdir = userprops.getStorage();
			var cleaned = (userdir + "/" + Objects.requireNonNull(user).getNeptunId() + "/projects/" + project.get().getIdentifier()).replace("//","/");
			var oldfile = new File(cleaned + '/' + decode(file) );
			var newfile = new File(cleaned + '/' + decode(projectFile.getName()) );
			
			if ( oldfile.exists() ) {
				if ( oldfile.renameTo(newfile) ) {
					return ResponseEntity.status(HttpStatus.ACCEPTED).build();
				}
			} 
		}
		return ResponseEntity.notFound().build();
	}

	@PutMapping("/create/directory/**")
	@SneakyThrows
	public ResponseEntity<StatusResponse> createDirectory(
			HttpServletRequest request,
			@PathVariable("projectid") Long projectId,
			@Valid @RequestBody ProjectFile projectFile
	) {
		var directory = "";
		var data = request.getRequestURI().split(request.getContextPath() + "/directory/");

		if ( data.length > 1 )
			directory = data[1];

		var user = userService.curr();
		var project = projectService.findByUserAndProjectId(user, projectId);

		if ( project.isPresent() && !BasicSecureFunctions.directoryTraversalInputCheckStartsWith(directory)
			&& projectFile.getType() == ProjectFileType.DIR
		) {
			var userdir = userprops.getStorage();
			var cleaned = (userdir + "/" + Objects.requireNonNull(user).getNeptunId() + "/projects/" + project.get().getIdentifier()).replace("//","/");
			var currdir = new File( cleaned + '/' + decode(directory) + '/' + decode(projectFile.getName()) );

			if ( !currdir.exists() )
				FileUtils.forceMkdir(currdir);

			return ResponseEntity.status(HttpStatus.ACCEPTED).build();
		}

		return ResponseEntity.notFound().build();
	}

	@PutMapping("/rename/directory/**")
	@SneakyThrows
	public ResponseEntity<StatusResponse> renameDirectory(
			HttpServletRequest request,
			@PathVariable("projectid") Long projectId,
			@Valid @RequestBody ProjectFile projectFile
	) {
		var directory = "";
		var data = request.getRequestURI().split(request.getContextPath() + "/directory/");

		if ( data.length > 1 )
			directory = data[1];

		var user = userService.curr();
		var project = projectService.findByUserAndProjectId(user, projectId);

		if ( project.isPresent() && !BasicSecureFunctions.directoryTraversalInputCheckStartsWith(directory)
				&& data.length > 1 && !BasicSecureFunctions.directoryTraversalInputCheckStartsWith(projectFile.getName())
				&& projectFile.getType() == ProjectFileType.DIR
		) {
			var userdir = userprops.getStorage();
			var cleaned = (userdir + "/" + Objects.requireNonNull(user).getNeptunId() + "/projects/" + project.get().getIdentifier()).replace("//","/");
			var newdir =  new File(cleaned + '/' +  decode( ( projectFile.getName() ) ) );
			var currdir = new File(cleaned + '/' +  decode( directory ) );

			if ( projectFile.getParent() != null &&  !projectFile.getParent().equals("") )
				newdir = new File(cleaned + '/' + decode((projectFile.getParent())) + '/' + decode(projectFile.getName())  );

			if ( currdir.exists() )
				FileUtils.moveDirectoryToDirectory(currdir, newdir , true);

			return ResponseEntity.status(HttpStatus.ACCEPTED).build();
		}
		return ResponseEntity.notFound().build();
	}


	@DeleteMapping("/remove/directory/**")
	@SneakyThrows
	public ResponseEntity<StatusResponse> removeDirectory(
			HttpServletRequest request,
			@PathVariable("projectid") Long projectId
	) {
		var directory = "";
		var data = request.getRequestURI().split(request.getContextPath() + "/directory/");

		if ( data.length > 1 )
			directory = data[1];

		var user = userService.curr();
		var project = projectService.findByUserAndProjectId(user, projectId);

		if ( project.isPresent() && !BasicSecureFunctions.directoryTraversalInputCheckStartsWith(directory)
				&& data.length > 1
		) {
			var userdir = userprops.getStorage();
			var cleaned = (userdir + "/" + Objects.requireNonNull(user).getNeptunId() + "/projects/" + project.get().getIdentifier()).replace("//","/");
			var currdir = new File(cleaned + '/' + decode( directory ) );

			if ( currdir.exists() )
				FileUtils.forceDelete(currdir);

			return ResponseEntity.status(HttpStatus.ACCEPTED).build();
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
			var cleaned = (userdir + "/" + Objects.requireNonNull(user).getNeptunId() + "/projects/" + project.get().getIdentifier() + String.format("/%s", decode(file))).replace("//","/");
			var ffile = new File( cleaned );
			
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
			var appdir = new StringBuilder( userdir + "/" + Objects.requireNonNull(user).getNeptunId() +  "/projects/" + project.get().getIdentifier() );
			var files = Lists.newArrayList();
			
			if ( dir.isPresent() ) { 
				var folder = dir.get();
				if ( folder.startsWith("/") ) {
					appdir.append(String.format("%s", decode(dir.get())));
				} else {
					appdir.append(String.format("/%s", decode(dir.get())));
				}
			}
			
	        try (Stream<Path> paths = Files.walk(Paths.get(appdir.toString()),1)) {
	            paths
	            	.filter(  e -> !e.getFileName().toString().equals(project.get().getIdentifier()) )
	            	.forEach( e -> { 
	            		 if ( Files.isDirectory(e) ) {
							 var cleaned = appdir.toString().replace("//","/");
	            			 if ( !e.toFile().getAbsolutePath().equals(cleaned) )
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
