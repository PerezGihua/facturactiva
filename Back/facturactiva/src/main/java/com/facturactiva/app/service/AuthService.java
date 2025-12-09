package com.facturactiva.app.service;

import com.facturactiva.app.dto.LoginRequest;
import com.facturactiva.app.dto.LoginResponse;
import com.facturactiva.app.dto.RegisterRequest;
import com.facturactiva.app.dto.RegisterResponse;
import com.facturactiva.app.dto.UserDetailsDTO;
import com.facturactiva.app.entity.UsuarioEntity;

import java.util.Map;
import java.util.Optional;

public interface AuthService {
    
    /**
     * Autentica un usuario y genera un token JWT
     */
    LoginResponse login(LoginRequest loginRequest);
    
    /**
     * Registra un nuevo usuario en el sistema
     */
    RegisterResponse register(RegisterRequest registerRequest);
    
    /**
     * Cierra la sesión del usuario actual
     */
    void logout();
    
    /**
     * Valida las credenciales de un usuario
     */
    Map<String, Object> validateUser(String email, String password);
    
    /**
     * Busca un usuario por su email
     */
    Optional<UsuarioEntity> findByEmail(String email);
}