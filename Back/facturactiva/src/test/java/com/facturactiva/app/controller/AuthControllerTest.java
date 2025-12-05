package com.facturactiva.app.controller;

import com.facturactiva.app.dto.LoginRequest;
import com.facturactiva.app.dto.LoginResponse;
import com.facturactiva.app.service.FacturactivaService;
import com.facturactiva.app.util.Constantes;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@DisplayName("Tests para AuthController")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private FacturactivaService authService;
    
    private LoginRequest validRequest;
    private LoginResponse successResponse;
    
    @BeforeEach
    void setUp() {
        // Usar el constructor correcto - solo username y password
        validRequest = new LoginRequest("admin", "password123", null, null);
        successResponse = new LoginResponse("1", "Admin User", Constantes.MSG_AUTH_SUCCESS);
    }
    
    @Test
    @DisplayName("Debe autenticar usuario correctamente con credenciales válidas")
    void debeAutenticarUsuarioConCredencialesValidas() throws Exception {
        when(authService.authenticateUser(any(LoginRequest.class)))
            .thenReturn(successResponse);
        
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idRol").value("1"))
                .andExpect(jsonPath("$.nombreUser").value("Admin User"))
                .andExpect(jsonPath("$.message").value(Constantes.MSG_AUTH_SUCCESS));
    }
    
    @Test
    @DisplayName("Debe retornar error con credenciales inválidas")
    void debeRetornarErrorConCredencialesInvalidas() throws Exception {
        LoginResponse failureResponse = new LoginResponse(null, null, Constantes.MSG_AUTH_FAILURE);
        
        when(authService.authenticateUser(any(LoginRequest.class)))
            .thenReturn(failureResponse);
        
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idRol").isEmpty())
                .andExpect(jsonPath("$.nombreUser").isEmpty())
                .andExpect(jsonPath("$.message").value(Constantes.MSG_AUTH_FAILURE));
    }
    
    @Test
    @DisplayName("Debe manejar request con username vacío")
    void debeManejarRequestConUsernameVacio() throws Exception {
        LoginRequest emptyUsernameRequest = new LoginRequest("", "password123", null, null);
        LoginResponse errorResponse = new LoginResponse(null, null, Constantes.INVALID_INPUT);
        
        when(authService.authenticateUser(any(LoginRequest.class)))
            .thenReturn(errorResponse);
        
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emptyUsernameRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(Constantes.INVALID_INPUT));
    }
    
    @Test
    @DisplayName("Debe manejar request con password vacío")
    void debeManejarRequestConPasswordVacio() throws Exception {
        LoginRequest emptyPasswordRequest = new LoginRequest("admin", "", null, null);
        LoginResponse errorResponse = new LoginResponse(null, null, Constantes.INVALID_INPUT);
        
        when(authService.authenticateUser(any(LoginRequest.class)))
            .thenReturn(errorResponse);
        
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emptyPasswordRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(Constantes.INVALID_INPUT));
    }
    
    @Test
    @DisplayName("Debe manejar error de base de datos")
    void debeManejarErrorDeBaseDatos() throws Exception {
        LoginResponse errorResponse = new LoginResponse(null, null, Constantes.DATABASE_ERROR);
        
        when(authService.authenticateUser(any(LoginRequest.class)))
            .thenReturn(errorResponse);
        
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(Constantes.DATABASE_ERROR));
    }
    
    @Test
    @DisplayName("Debe aceptar Content-Type application/json")
    void debeAceptarContentTypeJSON() throws Exception {
        when(authService.authenticateUser(any(LoginRequest.class)))
            .thenReturn(successResponse);
        
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
}