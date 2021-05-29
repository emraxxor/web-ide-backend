package com.github.emraxxor.web.ide.service;

import com.github.emraxxor.web.ide.entities.User;
import com.github.emraxxor.web.ide.repositories.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

@SpringBootTest
class UserServiceTest {

    @Mock
    private UserRepository repository;

    @InjectMocks
    private UserService service;

    User validUser;


    @BeforeEach
    void setUp() {
        validUser = User.builder()
                .neptunId("test")
                .userId(1L)
                .userMail("mail")
                .userPassword("password")
                .build();

    }

    @AfterEach
    void tearDown() {
    }


    @Test
    void findUserByNeptunId() {
        BDDMockito.given( repository.findByNeptunId(BDDMockito.any()) ).willReturn( Optional.of(validUser) );
        Assertions.assertThat( service.findUserByNeptunId("test") ).isEqualTo(Optional.of(validUser));
        BDDMockito.then(repository).should().findByNeptunId(validUser.getNeptunId());
    }

    @Test
    void findUserByEmail() {
        BDDMockito.given( repository.findByUserMail(BDDMockito.any()) ).willReturn( Optional.of(validUser) );
        Assertions.assertThat( service.findUserByEmail("mail") ).isEqualTo(Optional.of(validUser));
        BDDMockito.then(repository).should().findByUserMail(validUser.getUserMail());
    }

    @Test
    void save() {
        BDDMockito.given( repository.save(BDDMockito.any()) ).willReturn( validUser );
        Assertions.assertThat( service.save(validUser) ).isEqualTo( validUser );
        BDDMockito.then(repository).should().save(validUser);
    }

    @Test
    void findById() {
        BDDMockito.given( repository.findById(BDDMockito.any()) ).willReturn( Optional.of(validUser) );
        Assertions.assertThat( service.findById(1L) ).isEqualTo(Optional.of(validUser));
        BDDMockito.then(repository).should().findById(validUser.getUserId());
    }
}