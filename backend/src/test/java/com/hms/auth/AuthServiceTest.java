package com.hms.auth;

import com.hms.auth.dto.request.RegisterRequest;
import com.hms.auth.service.AuthService;
import com.hms.auth.service.impl.AuthServiceImpl;
import com.hms.common.audit.AuditLogService;
import com.hms.common.enums.Role;
import com.hms.doctor.repository.DoctorRepository;
import com.hms.security.jwt.JwtUtil;
import com.hms.user.entity.User;
import com.hms.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private DoctorRepository doctorRepository;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(userRepository, passwordEncoder, authenticationManager, jwtUtil, auditLogService, doctorRepository);
    }

    @Test
    void registerAllowsSelectedRoleFromRegisterRequest() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("doctor1");
        request.setEmail("doctor1@example.com");
        request.setPassword("Secret@123");
        request.setRole(Role.DOCTOR);

        when(userRepository.existsByUsername("doctor1")).thenReturn(false);
        when(userRepository.existsByEmail("doctor1@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Secret@123")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(UUID.randomUUID());
            return u;
        });

        authService.register(request);

        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode("Secret@123");
        assertEquals(Role.DOCTOR, request.getRole());
    }
}
