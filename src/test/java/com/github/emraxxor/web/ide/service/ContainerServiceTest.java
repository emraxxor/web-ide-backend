package com.github.emraxxor.web.ide.service;

import com.github.emraxxor.web.ide.entities.Container;
import com.github.emraxxor.web.ide.entities.User;
import com.github.emraxxor.web.ide.repositories.ContainerRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.eq;

@SpringBootTest
class ContainerServiceTest {

    @Mock
    private ContainerRepository repository;

    @InjectMocks
    private ContainerService service;

    private User validUser;

    private Container validContainer;

    @BeforeEach
    void setUp() {
        validUser = User.builder()
                .neptunId("test")
                .userId(1L)
                .userMail("mail")
                .userPassword("password")
                .build();

        validContainer = Container
                .builder()
                .containerId("container-id")
                .id(1L)
                .appdir("appdir")
                .exposed(4000)
                .bind(3000)
                .ip("ip")
                .build();
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void findByBind() {
        BDDMockito.given( repository.findByBind(BDDMockito.anyInt()) ).willReturn(Optional.of(validContainer));
        Assertions.assertThat( service.findByBind(3000)  ).isEqualTo( Optional.of(validContainer));
        BDDMockito.verify(repository).findByBind(validContainer.getBind());
    }


    @Test
    void findByContainerIdAndUser() {
        BDDMockito.given( repository.findByContainerIdAndProject_user(BDDMockito.anyString(), BDDMockito.any()) ).willReturn(Optional.of(validContainer));
        Assertions.assertThat( service.findByContainerIdAndUser("container-id", validUser)  ).isEqualTo( Optional.of(validContainer));
        BDDMockito.verify(repository).findByContainerIdAndProject_user(validContainer.getContainerId(), validUser );
    }

    @Test
    void invalidContainerIdAndUser() {
        BDDMockito.given( repository.findByContainerIdAndProject_user(eq(validContainer.getContainerId()), BDDMockito.any()) ).willReturn(Optional.of(validContainer));
        Assertions.assertThat( service.findByContainerIdAndUser("container-id-1", validUser)  ).isNotEqualTo( Optional.of(validContainer));
        BDDMockito.verify(repository).findByContainerIdAndProject_user(validContainer.getContainerId() + "-1", validUser );
    }

    @Test
    void findContainers() {
        var data = List.of(validContainer);
        BDDMockito.given( repository.findByProject_user(eq(validUser)) ).willReturn( data );
        Assertions.assertThat( service.findContainers(validUser )).isEqualTo( data );
        BDDMockito.verify(repository).findByProject_user( validUser );
    }
}