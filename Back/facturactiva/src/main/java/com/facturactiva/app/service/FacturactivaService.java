package com.facturactiva.app.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.facturactiva.app.dto.LoginRequest;
import com.facturactiva.app.dto.LoginResponse;
import com.facturactiva.app.entity.LoginEntity;
import com.facturactiva.app.util.Constantes;
import com.facturactiva.app.util.UtilClass;

@Service
public class FacturactivaService {
    
    private static final Logger logger = LogManager.getLogger(FacturactivaService.class);
    
    private LoginEntity authRepository;
    
    public LoginResponse authenticateUser(LoginRequest request) {
        String nomMetodo = Constantes.METODO_AUTHENTICATE_USER;
        
        logger.info("{} - Iniciando proceso de autenticación", nomMetodo);
        
        if (request == null) {
            logger.error("{} - Solicitud de autenticación nula", nomMetodo);
            return new LoginResponse(null, null, Constantes.INVALID_INPUT);
        }
        
        if (request.getUsername() == null || request.getUsername().isEmpty() ||
            request.getPassword() == null || request.getPassword().isEmpty()) {
            logger.warn("{} - Intento de login con campos vacíos", nomMetodo);
            return new LoginResponse(null, null, Constantes.INVALID_INPUT);
        }
        
        String username = request.getUsername();
        
        try {
            logger.info("{} - Autenticando usuario: {}", nomMetodo, username);
            
            String encodedPassword = UtilClass.encode(request.getPassword());
            logger.debug("{} - Password codificado exitosamente", nomMetodo);
            
            LoginResponse response = authRepository.verificarUsuario(username, encodedPassword);
            
            if (response == null || response.getIdRol() == null || response.getMessage() == null) {
                logger.warn("{} - Autenticación fallida para usuario: {}", nomMetodo, username);
                return new LoginResponse(null, null, Constantes.MSG_AUTH_FAILURE);
            }
            
            logger.info("{} - Usuario autenticado exitosamente: {} | Rol: {} | Nombre: {}", 
                       nomMetodo, username, response.getIdRol(), response.getNombreUser());
            
            return response;
            
        } catch (Exception e) {
            logger.error("{} - Error en autenticación para usuario: {} | Error: {}", 
                        nomMetodo, username, e.getMessage(), e);
            return new LoginResponse(null, null, Constantes.DATABASE_ERROR);
        }
    }
}