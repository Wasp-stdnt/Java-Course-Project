package io.github.wasp_stdnt.passwordmanagerv2.controller;

import io.github.wasp_stdnt.passwordmanagerv2.dto.UserRegistrationDto;
import io.github.wasp_stdnt.passwordmanagerv2.dto.UserResponseDto;
import io.github.wasp_stdnt.passwordmanagerv2.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@Validated
@Tag(name = "Users", description = "Register and manage local users (for admin/debug)")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(
            summary = "Register a new user",
            description = """
            Creates a new user in the local database.  
            Note: this endpoint is public, but real authentication is handled by Keycloak.
            """)
    @PostMapping
    public ResponseEntity<UserResponseDto> register(
            @Valid @RequestBody UserRegistrationDto registrationDto) {
        UserResponseDto userDto = userService.register(registrationDto);
        return ResponseEntity.ok(userDto);
    }

    @Operation(
            summary = "Get a user by ID",
            description = "Fetches user details (ID, name, email).")
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getById(@PathVariable Long id) {
        UserResponseDto userDto = userService.getById(id);
        return ResponseEntity.ok(userDto);
    }

    @Operation(
            summary = "Delete a user",
            description = "Deletes the user with the given ID from the local database.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
