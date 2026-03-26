package com.hms.user.controller;

import com.hms.user.dto.UserResponseDTO;
import com.hms.user.dto.UserSummary;
import com.hms.common.response.ApiResponse;
import com.hms.user.entity.User;
import com.hms.common.enums.Role;
import com.hms.user.repository.UserRepository;
import com.hms.doctor.repository.DoctorRepository;
import com.hms.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserResponseDTO>>> getAllUsers() {
        return ResponseEntity.ok(ApiResponse.success(userService.getAll()));
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserResponseDTO>> getCurrentUser() {
        return ResponseEntity.ok(ApiResponse.success(userService.getCurrentUser()));
    }

    @GetMapping("/eligible-doctors")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserSummary>> getEligibleDoctors() {
        List<User> doctorUsers = userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.DOCTOR)
                .filter(u -> !doctorRepository.existsByUserId(u.getId()))
                .toList();

        List<UserSummary> summaries = doctorUsers.stream()
                .map(u -> new UserSummary(u.getId().toString(), u.getUsername()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(summaries);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable("id") Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
