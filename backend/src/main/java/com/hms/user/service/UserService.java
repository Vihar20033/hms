package com.hms.user.service;

import com.hms.user.dto.UserResponseDTO;
import org.springframework.data.domain.Slice;

import java.util.List;

public interface UserService {
    List<UserResponseDTO> getAll();
    Slice<UserResponseDTO> getSlice(int page, int size);
    void deleteUser(Long id);
}
