package com.hms.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TokenType {
    ACCESS("Access Token"),
    REFRESH("Refresh Token");

    private final String description;
}
