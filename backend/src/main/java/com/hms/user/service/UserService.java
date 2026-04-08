package com.hms.user.service;

import com.hms.user.dto.UserResponseDTO;
import org.springframework.data.domain.Slice;

import java.util.List;

public interface UserService {
    List<UserResponseDTO> getAll();
    Slice<UserResponseDTO> getSlice(int page, int size);
    Slice<UserResponseDTO> getSlice(int page, int size, String query);
    void deleteUser(Long id);
}
