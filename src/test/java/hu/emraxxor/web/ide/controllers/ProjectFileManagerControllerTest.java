package hu.emraxxor.web.ide.controllers;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.BDDMockito.given;
import java.io.File;
import java.util.Optional;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.google.gson.Gson;

import hu.emraxxor.web.ide.config.UserProperties;
import hu.emraxxor.web.ide.data.type.project.ProjectFile;
import hu.emraxxor.web.ide.entities.Project;
import hu.emraxxor.web.ide.entities.User;
import hu.emraxxor.web.ide.repositories.ProjectRepository;
import hu.emraxxor.web.ide.service.ProjectService;
import hu.emraxxor.web.ide.service.UserService;

/**
 * 
 * @author Attila Barna
 *
 */
@ExtendWith(MockitoExtension.class)
@TestMethodOrder(OrderAnnotation.class)
class ProjectFileManagerControllerTest {

	@Mock
	ProjectService projectService;
	
	@Mock
	UserService userService;
	
	@Mock
	UserProperties userProperties;
	
	@InjectMocks
	ProjectFileManagerController fileManagerController;
	
	MockMvc mockMvc;
	
	
	ProjectRepository validRepository;
	
	User validUser;
	
	Project validProject;
	
	String encodedText;
	
	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		FileUtils.forceMkdir(new File("/tmp/test"));
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
		FileUtils.deleteDirectory(new File("/tmp/test"));
	}

	@BeforeEach
	void setUp() throws Exception {
		validUser = User.builder()
						.neptunId("test")
						.userId(1L)
						.build();
		
		validProject = Project
						.builder()
						.id(1L)
						.user(validUser)
						.identifier("datadir")
						.build();
		
		encodedText = Base64.encodeBase64String(new String("TEST").getBytes() );
		
		mockMvc = MockMvcBuilders.standaloneSetup(fileManagerController).build();
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	@Order(1) 
	void testCreate() throws Exception {
		when( userProperties.getStorage() ).thenReturn( "/tmp/test" );
		when( userService.curr() ).thenReturn( validUser );
		
		given(projectService.findByUserAndProjectId(BDDMockito.any(), BDDMockito.any()))
		 .willReturn(Optional.of(validProject));

		
		mockMvc.perform( 
				put("/api/project-filemanager/1")
				.contentType(MediaType.APPLICATION_JSON)
				.content(new Gson().toJson(
						ProjectFile
							.builder()
							.name("test")
							.data( encodedText )
				))
				.accept(MediaType.APPLICATION_JSON) )
				.andExpect(status().isCreated())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.code").value(1));
		
	}
	
	
	@Test
	@Order(2) 
	void testGet() throws Exception {
		when( userProperties.getStorage() ).thenReturn( "/tmp/test" );
		when( userService.curr() ).thenReturn( validUser );

		given(projectService.findByUserAndProjectId(BDDMockito.any(), BDDMockito.any()))
		.willReturn(Optional.of(validProject));

		put("/api/project-filemanager/1")
		.contentType(MediaType.APPLICATION_JSON)
		.content(new Gson().toJson(
				ProjectFile
					.builder()
					.name("test")
					.data( encodedText )
		));
		
		mockMvc.perform( get("/api/project-filemanager/1/file/test"))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.object.data").value(encodedText));
	}
	
	@Test
	@Order(3) 
	void testDelete() throws Exception {
		when( userProperties.getStorage() ).thenReturn( "/tmp/test" );
		when( userService.curr() ).thenReturn( validUser );

		given(projectService.findByUserAndProjectId(BDDMockito.any(), BDDMockito.any()))
		.willReturn(Optional.of(validProject));

		mockMvc.perform( delete("/api/project-filemanager/1/file/test"))
				.andExpect(status().isAccepted());
	}

}
