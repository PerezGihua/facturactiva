package com.facturactiva.app.exception;

import com.facturactiva.app.util.Constantes;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Log4j2
public class GlobalException {

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<Map<String, Object>> handleAuthenticationException(AuthException ex) {
        log.error("Error de autenticación: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, ex.getMessage(), Constantes.INVALID_CREDENTIALS);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentialsException(BadCredentialsException ex) {
        log.error("Credenciales inválidas: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, Constantes.MSG_AUTH_FAILURE, Constantes.INVALID_CREDENTIALS);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleUsernameNotFoundException(UsernameNotFoundException ex) {
        log.error("Usuario no encontrado: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, Constantes.MSG_USER_NOT_FOUND, Constantes.INVALID_CREDENTIALS);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        log.error("Error de validación de campos");
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "Validación fallida");
        response.put("errors", errors);
        response.put("code", Constantes.INVALID_INPUT);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        log.error("Error en tiempo de ejecución: {}", ex.getMessage(), ex);
        
        if (ex.getMessage() != null && ex.getMessage().contains(Constantes.DATABASE_ERROR)) {
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, 
                    "Error de base de datos", Constantes.DATABASE_ERROR);
        }
        
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Error interno del servidor", Constantes.UNKNOWN_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        log.error("Error no controlado: {}", ex.getMessage(), ex);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Error inesperado en el servidor", Constantes.UNKNOWN_ERROR);
    }

    private ResponseEntity<Map<String, Object>> buildErrorResponse(HttpStatus status, String message, String code) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", status.value());
        response.put("error", status.getReasonPhrase());
        response.put("message", message);
        response.put("code", code);
        return ResponseEntity.status(status).body(response);
    }
}