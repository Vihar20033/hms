package com.hms.auth;

import com.hms.common.audit.AuditLogService;
import com.hms.user.exception.EmailAlreadyExistsException;
import com.hms.user.exception.InvalidCredentialsException;
import com.hms.user.exception.UserNotFoundException;
import com.hms.user.exception.UsernameAlreadyExistsException;
import com.hms.security.jwt.JwtUtil;
import com.hms.user.entity.User;
import com.hms.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final AuditLogService auditLogService;

    public void register(RegisterRequest request) {

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UsernameAlreadyExistsException("Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setTokenVersion(0);
        user.setEnabled(true);
        user.setPasswordChangeRequired(false);

        userRepository.save(user);
    }

    public AuthResponse login(LoginRequest request) {

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()));
        } catch (BadCredentialsException e) {
            throw new InvalidCredentialsException("Invalid username or password");
        }

        User user = userRepository.findByUsername(request.getUsername())
                .or(() -> userRepository.findByEmail(request.getUsername()))
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        auditLogService.log(user.getUsername(), "USER_LOGIN", "User", user.getId().toString(),
                "role=" + user.getRole());

        return AuthResponse.builder()
                .token(jwtUtil.generateToken(user.getUsername(), user.getRole().name(), user.getTokenVersion()))
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .passwordChangeRequired(user.getPasswordChangeRequired())
                .build();
    }

    public void changePassword(ChangePasswordRequest request) {

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordChangeRequired(false);
        user.setTokenVersion(user.getTokenVersion() + 1);
        userRepository.save(user);
        auditLogService.log(username, "PASSWORD_CHANGE", "User", user.getId().toString(), null);
    }

    public void logout(String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        user.setTokenVersion(user.getTokenVersion() + 1);
        userRepository.save(user);
        auditLogService.log(username, "USER_LOGOUT", "User", user.getId().toString(), null);
    }
}
