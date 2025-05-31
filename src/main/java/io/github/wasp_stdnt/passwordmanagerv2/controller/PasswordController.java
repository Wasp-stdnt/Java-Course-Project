package io.github.wasp_stdnt.passwordmanagerv2.controller;

import io.github.wasp_stdnt.passwordmanagerv2.dto.PasswordResponseDto;
import io.github.wasp_stdnt.passwordmanagerv2.dto.PasswordWriteDto;
import io.github.wasp_stdnt.passwordmanagerv2.service.PasswordService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/passwords")
@Validated
public class PasswordController {
    private final PasswordService passwordService;

    public PasswordController(PasswordService passwordService) {
        this.passwordService = passwordService;
    }

    @PostMapping
    public ResponseEntity<PasswordResponseDto> createPassword(@Valid @RequestBody PasswordWriteDto createDto) {
        Long userId = getCurrentUserId();
        PasswordResponseDto responseDto = passwordService.createPassword(userId, createDto);
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping
    public ResponseEntity<List<PasswordResponseDto>> listPasswords() {
        Long userId = getCurrentUserId();
        List<PasswordResponseDto> passwords = passwordService.listPasswords(userId);
        return ResponseEntity.ok(passwords);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PasswordResponseDto> getPassword(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        PasswordResponseDto responseDto = passwordService.getPassword(userId, id);
        return ResponseEntity.ok(responseDto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PasswordResponseDto> updatePassword(
            @PathVariable Long id,
            @Valid @RequestBody PasswordWriteDto updateDto) {
        Long userId = getCurrentUserId();
        PasswordResponseDto responseDto = passwordService.updatePassword(userId, id, updateDto);
        return ResponseEntity.ok(responseDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePassword(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        passwordService.deletePassword(userId, id);
        return ResponseEntity.noContent().build();
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getPrincipal() instanceof Long
                ? (Long) auth.getPrincipal()
                : null;
    }
}
