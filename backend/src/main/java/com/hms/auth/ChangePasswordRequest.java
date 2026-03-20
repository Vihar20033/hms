package com.hms.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangePasswordRequest {

    @NotBlank
    @Size(min = 6, max = 128)
    private String currentPassword;

    @NotBlank
    @Size(min = 6, max = 128)
    private String newPassword;

    @AssertTrue(message = "New password must be different from current password")
    public boolean isNewPasswordDifferent() {
        if (currentPassword == null || newPassword == null) {
            return true;
        }
        return !currentPassword.equals(newPassword);
    }
}
