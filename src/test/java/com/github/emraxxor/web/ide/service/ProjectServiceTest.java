package com.github.emraxxor.web.ide.service;

import com.github.emraxxor.web.ide.config.UserProperties;
import com.github.emraxxor.web.ide.entities.Container;
import com.github.emraxxor.web.ide.entities.Project;
import com.github.emraxxor.web.ide.entities.User;
import com.github.emraxxor.web.ide.repositories.ProjectRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.eq;

@SpringBootTest
class ProjectServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private UserProperties userprops;

    @Mock
    private ProjectRepository repository;

    @Mock
    private DockerContainerService dockerContainerService;

    @InjectMocks
    private ProjectService service;

    private User validUser;

    private User invalidUser;

    private Container validContainer;

    private List<Project> projectList;

    @BeforeEach
    void setUp() {

        validUser = User.builder()
                .neptunId("test")
                .userId(1L)
                .userMail("mail")
                .userPassword("password")
                .build();

        invalidUser = User.builder()
                .neptunId("test")
                .userId(2L)
                .userMail("mail")
                .userPassword("password")
                .build();

        validContainer = Container
                                .builder()
                                .containerId("container-id")
                                .appdir("appdir")
                                .exposed(3000)
                                .ip("ip")
                                .build();

        projectList = Arrays.asList(
                    Project
                            .builder()
                            .id(1L)
                            .identifier("identifier")
                            .container(validContainer)
                            .user(validUser)
                            .build()
            );
    }

    @AfterEach
    void tearDown() {
    }


    @Test
    void projects() {
        BDDMockito.given( repository.findByUser_userId(BDDMockito.any()) ).willReturn( projectList );
        Assertions.assertThat( service.projects(validUser) ).isEqualTo( projectList );
        BDDMockito.then(repository).should().findByUser_userId(validUser.getUserId());
    }

    @Test
    void invalidProjects() {
        BDDMockito.given( repository.findByUser_userId( eq(validUser.getUserId()) ) ).willReturn( projectList );
        Assertions.assertThat( service.projects(invalidUser) ).isNotEqualTo( projectList );
        BDDMockito.then(repository).should().findByUser_userId(invalidUser.getUserId());
    }

    @Test
    void findByUserAndProjectId() {
        BDDMockito.given( repository.findByUserAndId(BDDMockito.any(), BDDMockito.anyLong()) ).willReturn( Optional.of(projectList.get(0)) );
        Assertions.assertThat( service.findByUserAndProjectId(validUser, 1L) ).isEqualTo( Optional.of(projectList.get(0)) );
        BDDMockito.then(repository).should().findByUserAndId(validUser, projectList.get(0).getId() );
    }

    @Test
    void invalidByUserAndProjectId() {
        BDDMockito.given( repository.findByUserAndId(eq(validUser), eq(projectList.get(0).getId())) ).willReturn( Optional.of(projectList.get(0)) );
        Assertions.assertThat( service.findByUserAndProjectId(validUser, 2L) ).isNotEqualTo( Optional.of(projectList.get(0)) );
        BDDMockito.then(repository).should().findByUserAndId(validUser, 2L );
    }

}