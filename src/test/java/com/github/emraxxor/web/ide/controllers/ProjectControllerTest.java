package com.github.emraxxor.web.ide.controllers;

import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.Container;
import com.github.emraxxor.web.ide.service.DockerContainerService;
import com.google.gson.Gson;
import com.github.emraxxor.web.ide.data.type.ProjectFormDeleteElement;
import com.github.emraxxor.web.ide.data.type.ProjectFormElement;
import com.github.emraxxor.web.ide.data.type.docker.DockerContainerInspectResponse;
import com.github.emraxxor.web.ide.entities.Project;
import com.github.emraxxor.web.ide.entities.User;
import com.github.emraxxor.web.ide.service.ProjectService;
import com.github.emraxxor.web.ide.service.UserService;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ProjectControllerTest {

    @Mock
    ProjectService projectService;

    @Mock
    UserService userService;

    @Mock
    ModelMapper mapper;

    @InjectMocks
    ProjectController projectController;

    MockMvc mockMvc;

    User validUser;

    Project validProject;

    @Mock
    DockerContainerService dockerContainerService;

    @BeforeEach
    void setUp() {
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

        mockMvc = MockMvcBuilders.standaloneSetup(projectController).build();
    }

    @AfterEach
    void tearDown() {
    }


    @Test
    void store() throws Exception  {
        given(projectService.create(BDDMockito.any())).willReturn(validProject);

        var elem = new ProjectFormElement();
        elem.setName("Project");

        mockMvc.perform(
                post("/api/project")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new Gson().toJson(elem))
                        .accept(MediaType.APPLICATION_JSON) )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(1));

    }

    @Test
    void delete() throws Exception {
        given(projectService.delete(new ProjectFormDeleteElement(validProject.getId()))).willReturn(true);

        var elem = new ProjectFormElement();
        elem.setName("Project");

        mockMvc.perform(
                MockMvcRequestBuilders.delete("/api/project/" + validProject.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON) )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(1));

    }

    @Test
    void startProjectContainer() throws Exception {
        when(dockerContainerService.inspect(validProject)).thenReturn(new InspectContainerResponse());
        when(mapper.map(BDDMockito.any(),BDDMockito.any())).thenReturn(new DockerContainerInspectResponse());
        when(dockerContainerService.start(validProject)).thenReturn(true);
        given(projectService.findByUserAndProjectId(BDDMockito.any(),BDDMockito.anyLong())).willReturn(Optional.of(validProject));
        mockMvc.perform(
                post("/api/project/start/" + validProject.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON) )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(1));

    }

    @Test
    void projects() throws Exception {

        class ContainerMockType extends Container {
            @Override
            public String[] getNames() {
                return new String[] {"name-1","name-2"};
            }
        }

        when(dockerContainerService.containers()).thenReturn(Lists.newArrayList(new ContainerMockType()));
        when(userService.curr()).thenReturn(validUser);
        given(projectService.projects(BDDMockito.any(User.class))).willReturn(Lists.newArrayList(validProject));
        mockMvc.perform(
                get("/api/project" )
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON) )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(1));

    }
}