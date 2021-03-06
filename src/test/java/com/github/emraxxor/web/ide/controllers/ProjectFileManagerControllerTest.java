package com.github.emraxxor.web.ide.controllers;

import com.github.emraxxor.web.ide.config.UserProperties;
import com.github.emraxxor.web.ide.data.type.project.ProjectFile;
import com.github.emraxxor.web.ide.entities.Project;
import com.github.emraxxor.web.ide.entities.User;
import com.github.emraxxor.web.ide.service.ProjectService;
import com.github.emraxxor.web.ide.service.UserService;
import com.google.gson.Gson;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.File;
import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 
 * @author Attila Barna
 *
 */
@ExtendWith(MockitoExtension.class)
@TestMethodOrder(OrderAnnotation.class)
class ProjectFileManagerControllerTest {

	@Mock
	private ProjectService projectService;
	
	@Mock
	private UserService userService;
	
	@Mock
	private UserProperties userProperties;
	
	@InjectMocks
	private ProjectFileManagerController fileManagerController;

	private MockMvc mockMvc;

	private User validUser;

	private Project validProject;

	private String encodedText;
	
	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		FileUtils.forceMkdir(new File("/tmp/test"));
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
		FileUtils.deleteDirectory(new File("/tmp/test"));
	}

	@BeforeEach
	void setUp()  {
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
		
		encodedText = Base64.encodeBase64String("TEST".getBytes() );
		
		mockMvc = MockMvcBuilders.standaloneSetup(fileManagerController).build();
	}

	@AfterEach
	void tearDown() {
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
