package com.hms.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Standard Token Types for JWT-based Authentication
 */
@Getter
@RequiredArgsConstructor
public enum TokenType {
    ACCESS("Access Token"),
    REFRESH("Refresh Token");

    private final String description;
}
