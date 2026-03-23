package com.hms.user.service.impl;

import com.hms.user.dto.UserResponseDTO;
import com.hms.user.entity.User;
import com.hms.user.mapper.UserMapper;
import com.hms.user.repository.UserRepository;
import com.hms.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Current user not found"));
        return userMapper.toResponseDTO(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDTO> getAll() {
        return userRepository.findAll().stream()
                .filter(user -> !user.isDeleted())
                .map(userMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteUser(UUID id) {
        User user = userRepository.findById(id)
                .filter(existingUser -> !existingUser.isDeleted())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        retireUserIdentity(user);
        user.setDeleted(true);
        user.setEnabled(false);
        userRepository.save(user);
    }

    @Override
    public void retireUserIdentity(User user) {
        String uniqueSuffix = "__deleted__" + user.getId();

        if (user.getUsername() != null && !user.getUsername().contains("__deleted__")) {
            String retiredUsername = user.getUsername() + uniqueSuffix;
            user.setUsername(truncate(retiredUsername));
        }

        if (user.getEmail() != null && !user.getEmail().contains("__deleted__")) {
            user.setEmail(user.getId() + "__deleted__" + user.getEmail());
        }
    }

    private String truncate(String value) {
        if (value == null || value.length() <= 50) {
            return value;
        }
        return value.substring(0, 50);
    }
}
