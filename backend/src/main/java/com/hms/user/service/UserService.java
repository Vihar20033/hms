package com.hms.user.service;

import com.hms.user.dto.UserResponseDTO;

import java.util.List;

public interface UserService {
    List<UserResponseDTO> getAll();
    void deleteUser(Long id);
}
