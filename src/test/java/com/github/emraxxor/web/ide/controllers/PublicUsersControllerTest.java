package com.github.emraxxor.web.ide.controllers;

import com.github.emraxxor.web.ide.data.type.UserFormElement;
import com.github.emraxxor.web.ide.entities.User;
import com.github.emraxxor.web.ide.service.UserService;
import com.google.gson.Gson;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PublicUsersControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private ModelMapper mapper;

    @Mock
    private PasswordEncoder encoder;

    @InjectMocks
    private PublicUsersController publicUsersController;

    private MockMvc mockMvc;

    private User validUser;

    @BeforeEach
    void setUp() {
        validUser = User.builder()
                .neptunId("test")
                .userId(1L)
                .build();

        mockMvc = MockMvcBuilders.standaloneSetup(publicUsersController).build();
    }

    @AfterEach
    void tearDown() {
    }


    @Test
    void registration() throws Exception {
        given(userService.findUserByEmail(BDDMockito.anyString())).willReturn(Optional.empty());
        given(mapper.map(BDDMockito.any(),BDDMockito.any())).willReturn(validUser);
        mockMvc.perform(
                         post("/users" )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new Gson().toJson(
                            UserFormElement
                                    .builder()
                                    .neptunId("NeptunId")
                                    .userPassword("UserPassword")
                                    .userMail("mail@test.com")
                                    .firstName("FirstName")
                                    .lastName("LastName")
                                    .build())
                        )
                        .accept(MediaType.APPLICATION_JSON) )
                        .andExpect(status().isCreated())
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.code").value(1));

    }

    @Test
    void invalidRegistration() throws Exception {
        mockMvc.perform(
                post("/users" )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new Gson().toJson(
                                UserFormElement
                                        .builder()
                                        .userPassword("UserPassword")
                                        .firstName("FirstName")
                                        .lastName("LastName")
                                        .build())
                        )
                        .accept(MediaType.APPLICATION_JSON) )
                .andExpect(status().isBadRequest());

    }


}