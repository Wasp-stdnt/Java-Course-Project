package io.github.wasp_stdnt.passwordmanagerv2.controller;

import io.github.wasp_stdnt.passwordmanagerv2.dto.PasswordResponseDto;
import io.github.wasp_stdnt.passwordmanagerv2.dto.PasswordWriteDto;
import io.github.wasp_stdnt.passwordmanagerv2.security.CurrentUser;
import io.github.wasp_stdnt.passwordmanagerv2.service.PasswordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/passwords")
@Validated
@Tag(name = "Passwords", description = "Create, read, update, and delete password entries")
public class PasswordController {
    private final PasswordService passwordService;

    public PasswordController(PasswordService passwordService) {
        this.passwordService = passwordService;
    }

    @Operation(
            summary = "Create a new password",
            description = """
            Encrypts the provided plaintext password and stores it under the authenticated user.
            
            **Request Body** must include:
            - `service`: the name of the service (e.g., "Gmail")
            - `credential`: your username or email for that service
            - `password`: the plaintext password to encrypt
            """)
    @PostMapping
    public ResponseEntity<PasswordResponseDto> createPassword(
            @Valid @RequestBody PasswordWriteDto createDto,
            @CurrentUser Long userId) {
        PasswordResponseDto responseDto = passwordService.createPassword(userId, createDto);
        return ResponseEntity.ok(responseDto);
    }

    @Operation(
            summary = "List all passwords",
            description = "Returns an array of stored passwords for the authenticated user. Decrypted plaintext values are included.")
    @GetMapping
    public ResponseEntity<List<PasswordResponseDto>> listPasswords(@CurrentUser Long userId) {
        List<PasswordResponseDto> passwords = passwordService.listPasswords(userId);
        return ResponseEntity.ok(passwords);
    }

    @Operation(
            summary = "Get a single password",
            description = "Fetches one password entry by its ID. The ID must belong to the authenticated user.")
    @GetMapping("/{id}")
    public ResponseEntity<PasswordResponseDto> getPassword(
            @PathVariable Long id,
            @CurrentUser Long userId) {
        PasswordResponseDto responseDto = passwordService.getPassword(userId, id);
        return ResponseEntity.ok(responseDto);
    }

    @Operation(
            summary = "Update an existing password",
            description = """
            Updates (re-encrypts) the password entry with the given ID.
            Request Body is the same as **Create**, except that the `id` path must match an existing record.
            """)
    @PutMapping("/{id}")
    public ResponseEntity<PasswordResponseDto> updatePassword(
            @PathVariable Long id,
            @Valid @RequestBody PasswordWriteDto updateDto,
            @CurrentUser Long userId) {
        PasswordResponseDto responseDto = passwordService.updatePassword(userId, id, updateDto);
        return ResponseEntity.ok(responseDto);
    }

    @Operation(
            summary = "Delete a password",
            description = "Permanently deletes the password entry identified by the given ID.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePassword(
            @PathVariable Long id,
            @CurrentUser Long userId) {
        passwordService.deletePassword(userId, id);
        return ResponseEntity.noContent().build();
    }
}
