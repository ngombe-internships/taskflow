package com.duva.taskflow.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

// Global Exception Handler - Centralise la gestion des erreurs  Gère automatiquement TOUS les RuntimeException
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Gère TOUS les RuntimeException (includes custom messages)

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {

        // Détermine le status code basé sur le message
        HttpStatus status = determineStatus(ex.getMessage());

        Map<String, Object> response = new HashMap<>();
        response.put("error", status.getReasonPhrase());
        response.put("message", ex.getMessage());
        response.put("timestamp", LocalDateTime.now());
        response.put("status", status.value());

        return ResponseEntity.status(status).body(response);
    }

    //Gère les erreurs de validation Spring

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationError(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult()
                .getFieldError()
                .getDefaultMessage();

        Map<String, Object> response = new HashMap<>();
        response.put("error", "Bad Request");
        response.put("message", message);
        response.put("timestamp", LocalDateTime.now());
        response.put("status", 400);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // Gère les erreurs d'authentification Spring Security

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(BadCredentialsException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Unauthorized");
        response.put("message", "Invalid email or password");
        response.put("timestamp", LocalDateTime.now());
        response.put("status", 401);

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    // Détermine le HTTP status code basé sur le message d'erreur

    private HttpStatus determineStatus(String message) {
        if (message == null) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }

        message = message.toLowerCase();

        if (message.contains("not found")) {
            return HttpStatus.NOT_FOUND;  // 404
        }
        if (message.contains("already exists")) {
            return HttpStatus.CONFLICT;  // 409
        }
        if (message.contains("permission") || message.contains("unauthorized")) {
            return HttpStatus.FORBIDDEN;  // 403
        }
        if (message.contains("token") && message.contains("invalid")) {
            return HttpStatus.UNAUTHORIZED;  // 401
        }

        return HttpStatus.INTERNAL_SERVER_ERROR;  // 500
    }
}