package io.github.wasp_stdnt.passwordmanagerv2.controller;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.wasp_stdnt.passwordmanagerv2.dto.PasswordResponseDto;
import io.github.wasp_stdnt.passwordmanagerv2.dto.PasswordWriteDto;
import io.github.wasp_stdnt.passwordmanagerv2.security.JwtService;
import io.github.wasp_stdnt.passwordmanagerv2.service.PasswordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@ExtendWith(SpringExtension.class)
@WebMvcTest(PasswordController.class)
@Import(io.github.wasp_stdnt.passwordmanagerv2.config.SecurityConfig.class)
class PasswordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PasswordService passwordService;

    @MockitoBean
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    private UsernamePasswordAuthenticationToken auth;
    private DecodedJWT mockDecodedJwt;

    @BeforeEach
    void setUp() {
        auth = new UsernamePasswordAuthenticationToken(1L, null, List.of());

        when(jwtService.validateToken(anyString())).thenReturn(true);

        mockDecodedJwt = Mockito.mock(DecodedJWT.class);
        when(mockDecodedJwt.getSubject()).thenReturn("1");

        when(jwtService.getDecodedToken(anyString())).thenReturn(mockDecodedJwt);
    }

    @Test
    void createPassword_shouldReturnCreatedDto() throws Exception {
        PasswordWriteDto createDto = PasswordWriteDto.builder()
                .service("Gmail")
                .credential("alice@gmail.com")
                .password("secret")
                .build();

        PasswordResponseDto responseDto = PasswordResponseDto.builder()
                .id(2L)
                .service("Gmail")
                .credential("alice@gmail.com")
                .password("secret")
                .build();

        when(passwordService.createPassword(eq(1L), ArgumentMatchers.any(PasswordWriteDto.class)))
                .thenReturn(responseDto);

        mockMvc.perform(post("/api/passwords")
                        .with(authentication(auth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$.service").value("Gmail"))
                .andExpect(jsonPath("$.credential").value("alice@gmail.com"))
                .andExpect(jsonPath("$.password").value("secret"));

        verify(passwordService).createPassword(eq(1L), ArgumentMatchers.any(PasswordWriteDto.class));
    }

    @Test
    void listPasswords_shouldReturnList() throws Exception {
        PasswordResponseDto dto1 = PasswordResponseDto.builder()
                .id(2L)
                .service("Gmail")
                .credential("alice@gmail.com")
                .password("secret")
                .build();

        when(passwordService.listPasswords(1L)).thenReturn(List.of(dto1));

        mockMvc.perform(get("/api/passwords")
                        .with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(2L))
                .andExpect(jsonPath("$[0].service").value("Gmail"))
                .andExpect(jsonPath("$[0].credential").value("alice@gmail.com"))
                .andExpect(jsonPath("$[0].password").value("secret"));

        verify(passwordService).listPasswords(1L);
    }

    @Test
    void getPassword_shouldReturnSingleDto() throws Exception {
        PasswordResponseDto responseDto = PasswordResponseDto.builder()
                .id(2L)
                .service("Gmail")
                .credential("alice@gmail.com")
                .password("secret")
                .build();

        when(passwordService.getPassword(1L, 2L)).thenReturn(responseDto);

        mockMvc.perform(get("/api/passwords/2")
                        .with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$.service").value("Gmail"))
                .andExpect(jsonPath("$.credential").value("alice@gmail.com"))
                .andExpect(jsonPath("$.password").value("secret"));

        verify(passwordService).getPassword(1L, 2L);
    }

    @Test
    void updatePassword_shouldReturnUpdatedDto() throws Exception {
        PasswordWriteDto updateDto = PasswordWriteDto.builder()
                .service("Google")
                .credential("alice.new@gmail.com")
                .password("newsecret")
                .build();

        PasswordResponseDto responseDto = PasswordResponseDto.builder()
                .id(2L)
                .service("Google")
                .credential("alice.new@gmail.com")
                .password("newsecret")
                .build();

        when(passwordService.updatePassword(eq(1L), eq(2L), ArgumentMatchers.any(PasswordWriteDto.class)))
                .thenReturn(responseDto);

        mockMvc.perform(put("/api/passwords/2")
                        .with(authentication(auth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$.service").value("Google"))
                .andExpect(jsonPath("$.credential").value("alice.new@gmail.com"))
                .andExpect(jsonPath("$.password").value("newsecret"));

        verify(passwordService).updatePassword(eq(1L), eq(2L), ArgumentMatchers.any(PasswordWriteDto.class));
    }

    @Test
    void deletePassword_shouldReturnNoContent() throws Exception {
        doNothing().when(passwordService).deletePassword(1L, 2L);

        mockMvc.perform(delete("/api/passwords/2")
                        .with(authentication(auth)))
                .andExpect(status().isNoContent());

        verify(passwordService).deletePassword(1L, 2L);
    }
}
