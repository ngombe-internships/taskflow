package com.duva.taskflow.exception;

// Exception personnalisée pour quand un rôle n'existe pas
public class RoleNotFoundException extends RuntimeException {
    public RoleNotFoundException(String message) {
        super(message);
    }

    public RoleNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}