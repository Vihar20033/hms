package com.hms.common.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)

// Generic wrapper class for all APIs response
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private String errorCode;
    private T data;
    private int status;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
    private String path;
    private List<ValidationError> validationErrors;

    public static <T> ApiResponse<T> success(T data) {
        return success(data, "Request successful", HttpStatus.OK);
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return success(data, message, HttpStatus.OK);
    }

    public static <T> ApiResponse<T> success(T data, String message, HttpStatus httpStatus) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setMessage(message);
        response.setData(data);
        response.setStatus(httpStatus.value());
        response.setTimestamp(LocalDateTime.now());
        return response;
    }
}
