package io.github.wasp_stdnt.passwordmanagerv2.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.wasp_stdnt.passwordmanagerv2.dto.AuthResponseDto;
import io.github.wasp_stdnt.passwordmanagerv2.dto.LoginRequestDto;
import io.github.wasp_stdnt.passwordmanagerv2.dto.UserRegistrationDto;
import io.github.wasp_stdnt.passwordmanagerv2.dto.UserResponseDto;
import io.github.wasp_stdnt.passwordmanagerv2.exception.ConflictException;
import io.github.wasp_stdnt.passwordmanagerv2.exception.GlobalExceptionHandler;
import io.github.wasp_stdnt.passwordmanagerv2.exception.NotFoundException;
import io.github.wasp_stdnt.passwordmanagerv2.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private UserService userService;

    @BeforeEach
    void setUp() {
        UserController controller = new UserController(userService);
        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("POST /api/users → successful registration → 200 OK + returned UserResponseDto")
    void register_success() throws Exception {
        UserRegistrationDto registrationDto = UserRegistrationDto.builder()
                .name("Alice")
                .email("alice@example.com")
                .password("password123")
                .build();

        UserResponseDto serviceResponse = UserResponseDto.builder()
                .id(1L)
                .name("Alice")
                .email("alice@example.com")
                .build();

        when(userService.register(any(UserRegistrationDto.class)))
                .thenReturn(serviceResponse);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Alice")))
                .andExpect(jsonPath("$.email", is("alice@example.com")));

        ArgumentCaptor<UserRegistrationDto> captor = ArgumentCaptor.forClass(UserRegistrationDto.class);
        Mockito.verify(userService).register(captor.capture());
        UserRegistrationDto passed = captor.getValue();
        assert passed.getEmail().equals("alice@example.com");
    }

    @Test
    @DisplayName("POST /api/users → email already in use → 409 Conflict")
    void register_conflictEmail() throws Exception {
        UserRegistrationDto registrationDto = UserRegistrationDto.builder()
                .name("Bob")
                .email("bob@example.com")
                .password("pass12345")
                .build();

        when(userService.register(any(UserRegistrationDto.class)))
                .thenThrow(new ConflictException("Email already in use"));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationDto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code", is("CONFLICT")))
                .andExpect(jsonPath("$.message", is("Email already in use")));
    }

    @Test
    @DisplayName("POST /api/users/login → valid credentials → 200 OK with AuthResponseDto")
    void login_success() throws Exception {
        LoginRequestDto loginDto = new LoginRequestDto("alice@example.com", "password123");

        UserResponseDto userDto = UserResponseDto.builder()
                .id(2L)
                .name("Alice")
                .email("alice@example.com")
                .build();

        AuthResponseDto authResponse = AuthResponseDto.builder()
                .token("dummy-jwt-token")
                .user(userDto)
                .build();

        when(userService.login(any(LoginRequestDto.class)))
                .thenReturn(authResponse);

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", is("dummy-jwt-token")))
                .andExpect(jsonPath("$.user.id", is(2)))
                .andExpect(jsonPath("$.user.email", is("alice@example.com")));
    }

    @Test
    @DisplayName("POST /api/users/login → wrong credentials → 409 Conflict")
    void login_invalidCredentials() throws Exception {
        LoginRequestDto loginDto = new LoginRequestDto("alice@example.com", "wrongpass");

        when(userService.login(any(LoginRequestDto.class)))
                .thenThrow(new ConflictException("Invalid credentials"));

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code", is("CONFLICT")))
                .andExpect(jsonPath("$.message", is("Invalid credentials")));
    }

    @Test
    @DisplayName("GET /api/users/{id} → found → 200 OK + UserResponseDto")
    void getById_success() throws Exception {
        UserResponseDto userDto = UserResponseDto.builder()
                .id(3L)
                .name("Charlie")
                .email("charlie@example.com")
                .build();

        when(userService.getById(3L)).thenReturn(userDto);

        mockMvc.perform(get("/api/users/{id}", 3L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(3)))
                .andExpect(jsonPath("$.name", is("Charlie")))
                .andExpect(jsonPath("$.email", is("charlie@example.com")));
    }

    @Test
    @DisplayName("GET /api/users/{id} → not found → 404 Not Found")
    void getById_notFound() throws Exception {
        when(userService.getById(5L))
                .thenThrow(new NotFoundException("User not found"));

        mockMvc.perform(get("/api/users/{id}", 5L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", is("NOT_FOUND")))
                .andExpect(jsonPath("$.message", is("User not found")));
    }

    @Test
    @DisplayName("DELETE /api/users/{id} → success → 204 No Content")
    void deleteUser_success() throws Exception {
        mockMvc.perform(delete("/api/users/{id}", 7L))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/users/{id} → not found → 404 Not Found")
    void deleteUser_notFound() throws Exception {
        doThrow(new NotFoundException("User not found")).when(userService).deleteUser(8L);

        mockMvc.perform(delete("/api/users/{id}", 8L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", is("NOT_FOUND")))
                .andExpect(jsonPath("$.message", is("User not found")));
    }
}
