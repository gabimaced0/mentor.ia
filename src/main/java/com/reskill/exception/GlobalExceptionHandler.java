package com.reskill.exception;

import com.reskill.exception.exceptions.ResourceAlreadyExistsException;
import com.reskill.exception.exceptions.ResourceNotFoundException;
import com.reskill.exception.exceptions.UnauthorizedAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private record ApiError(
            Instant timestamp,
            int status,
            String error,
            String message,
            Map<String, Object> details
    ) {}

    private ResponseEntity<ApiError> buildError(
            HttpStatus status,
            String error,
            String message,
            Map<String, Object> details
    ) {
        ApiError apiError = new ApiError(
                Instant.now(),
                status.value(),
                error,
                message,
                details
        );
        return ResponseEntity.status(status).body(apiError);
    }


    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<ApiError> handleAlreadyExists(ResourceAlreadyExistsException ex) {
        return buildError(
                HttpStatus.CONFLICT,
                "Resource Already Exists",
                ex.getMessage(),
                Map.of("param", ex.getMessage())
        );
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex) {
        return buildError(
                HttpStatus.NOT_FOUND,
                "Resource Not Found",
                ex.getMessage(),
                Map.of("param", ex.getMessage())
        );
    }

    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<ApiError> handleUnauthorized(UnauthorizedAccessException ex) {
        return buildError(
                HttpStatus.UNAUTHORIZED,
                "Unauthorized Access",
                ex.getMessage(),
                Map.of("param", ex.getMessage())
        );
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiError> handleGeneral(RuntimeException ex) {
        return buildError(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                ex.getMessage(),
                Map.of()
        );
    }
}
