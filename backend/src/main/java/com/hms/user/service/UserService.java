package com.hms.user.service;

import com.hms.user.dto.ChangePasswordRequest;
import com.hms.user.dto.UserResponseDTO;

public interface UserService {
    UserResponseDTO getCurrentUser();
    void changePassword(ChangePasswordRequest request);
}
