package com.hms.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiError {

    private boolean success;
    private String message;
    private String errorCode;
    private int status;
    private Instant timestamp;
    private String path;
    private List<ValidationError> validationErrors;

    public static ApiError of(String message, String errorCode, HttpStatus status) {
        return ApiError.builder()
                .success(false)
                .message(message)
                .errorCode(errorCode)
                .status(status.value())
                .timestamp(Instant.now())
                .build();
    }
}
