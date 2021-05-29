package com.github.emraxxor.web.ide.controllers;

import com.github.emraxxor.web.ide.data.type.UserFormElement;
import com.github.emraxxor.web.ide.data.type.UserProfilePersonalFormElement;
import com.github.emraxxor.web.ide.entities.User;
import com.github.emraxxor.web.ide.service.ProfileStorageService;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UsersControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private ProfileStorageService profileStorage;

    @Mock
    private ModelMapper mapper;

    @Mock
    private PasswordEncoder encoder;

    @InjectMocks
    private UsersController usersController;

    private MockMvc mockMvc;

    private User validUser;

    @BeforeEach
    void setUp() {
        validUser = User.builder()
                .neptunId("test")
                .userId(1L)
                .userMail("mail")
                .userPassword("password")
                .build();

        mockMvc = MockMvcBuilders.standaloneSetup(usersController).build();
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void info() throws Exception {
        given(userService.curr()).willReturn(validUser);
        mockMvc.perform(
                get("/api/user/info" )
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON) )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userId").value(validUser.getUserId()));

    }

    @Test
    void invalidUser() throws Exception {
        given(userService.curr()).willReturn(User.builder()
                .neptunId("test2")
                .userId(2L)
                .userMail("mail2")
                .userPassword("password2")
                .build());

        mockMvc.perform(
                get("/api/user/info" )
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON) )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userId").value(2));

    }

    @Test
    void update() throws Exception {
        when(userService.current()).thenReturn(Optional.of(validUser));
        when(userService.principal()).thenReturn(new UserFormElement(validUser));
        when(encoder.matches(BDDMockito.any(), BDDMockito.anyString())).thenReturn(true);

        given(userService.save(validUser)).willReturn(validUser);

        mockMvc.perform(
                put("/api/user/personal" )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new Gson().toJson(
                                UserProfilePersonalFormElement
                                        .builder()
                                        .userMail(validUser.getUserMail())
                                        .oldUserPassword(validUser.getUserPassword())
                                        .confirmUserPassword("password")
                                        .userPassword("password")
                                        .build()

                        ))
                        .accept(MediaType.APPLICATION_JSON) )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(1));
    }

    @Test
    void invalidUpdate() throws Exception {
        mockMvc.perform(
                put("/api/user/personal" )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new Gson().toJson(
                                UserProfilePersonalFormElement
                                        .builder()
                                        .userMail(validUser.getUserMail())
                                        .confirmUserPassword("password")
                                        .userPassword("password")
                                        .build()

                        ))
                        .accept(MediaType.APPLICATION_JSON) )
                .andExpect(status().isNotFound());
    }

}