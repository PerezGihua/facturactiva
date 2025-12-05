package com.facturactiva.app.service;

import com.facturactiva.app.dto.LoginRequest;
import com.facturactiva.app.dto.LoginResponse;
import com.facturactiva.app.entity.LoginEntity;
import com.facturactiva.app.util.Constantes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests para FacturactivaService")
class FacturactivaServiceTest {

    @Mock
    private LoginEntity authRepository;
    
    @InjectMocks
    private FacturactivaService service;
    
    private LoginRequest validRequest;
    private LoginResponse successResponse;
    
    @BeforeEach
    void setUp() {
        validRequest = new LoginRequest(null, null, "admin", "password123");
        successResponse = new LoginResponse("1", "Admin User", Constantes.MSG_AUTH_SUCCESS);
    }
    
    @Test
    @DisplayName("Debe autenticar usuario exitosamente con credenciales válidas")
    void debeAutenticarUsuarioExitosamenteConCredencialesValidas() {
        when(authRepository.verificarUsuario(anyString(), anyString()))
            .thenReturn(successResponse);
        
        LoginResponse response = service.authenticateUser(validRequest);
        
        assertThat(response).isNotNull();
        assertThat(response.getIdRol()).isEqualTo("1");
        assertThat(response.getNombreUser()).isEqualTo("Admin User");
        assertThat(response.getMessage()).isEqualTo(Constantes.MSG_AUTH_SUCCESS);
        
        verify(authRepository, times(1)).verificarUsuario(anyString(), anyString());
    }
    
    @Test
    @DisplayName("Debe retornar error cuando el request es nulo")
    void debeRetornarErrorCuandoElRequestEsNulo() {
        LoginResponse response = service.authenticateUser(null);
        
        assertThat(response).isNotNull();
        assertThat(response.getIdRol()).isNull();
        assertThat(response.getNombreUser()).isNull();
        assertThat(response.getMessage()).isEqualTo(Constantes.INVALID_INPUT);
        
        verify(authRepository, never()).verificarUsuario(anyString(), anyString());
    }
    
    @Test
    @DisplayName("Debe retornar error cuando username es nulo")
    void debeRetornarErrorCuandoUsernameEsNulo() {
        LoginRequest requestWithNullUsername = new LoginRequest(null, null, null, "password");
        
        LoginResponse response = service.authenticateUser(requestWithNullUsername);
        
        assertThat(response).isNotNull();
        assertThat(response.getMessage()).isEqualTo(Constantes.INVALID_INPUT);
        
        verify(authRepository, never()).verificarUsuario(anyString(), anyString());
    }
    
    @Test
    @DisplayName("Debe retornar error cuando username está vacío")
    void debeRetornarErrorCuandoUsernameEstaVacio() {
        LoginRequest requestWithEmptyUsername = new LoginRequest(null, null, "", "password");
        
        LoginResponse response = service.authenticateUser(requestWithEmptyUsername);
        
        assertThat(response).isNotNull();
        assertThat(response.getMessage()).isEqualTo(Constantes.INVALID_INPUT);
        
        verify(authRepository, never()).verificarUsuario(anyString(), anyString());
    }
    
    @Test
    @DisplayName("Debe retornar error cuando password es nulo")
    void debeRetornarErrorCuandoPasswordEsNulo() {
        LoginRequest requestWithNullPassword = new LoginRequest(null, null, "admin", null);
        
        LoginResponse response = service.authenticateUser(requestWithNullPassword);
        
        assertThat(response).isNotNull();
        assertThat(response.getMessage()).isEqualTo(Constantes.INVALID_INPUT);
        
        verify(authRepository, never()).verificarUsuario(anyString(), anyString());
    }
    
    @Test
    @DisplayName("Debe retornar error cuando password está vacío")
    void debeRetornarErrorCuandoPasswordEstaVacio() {
        LoginRequest requestWithEmptyPassword = new LoginRequest(null, null, "admin", "");
        
        LoginResponse response = service.authenticateUser(requestWithEmptyPassword);
        
        assertThat(response).isNotNull();
        assertThat(response.getMessage()).isEqualTo(Constantes.INVALID_INPUT);
        
        verify(authRepository, never()).verificarUsuario(anyString(), anyString());
    }
    
    @Test
    @DisplayName("Debe retornar error cuando la respuesta del repositorio es nula")
    void debeRetornarErrorCuandoLaRespuestaDelRepositorioEsNula() {
        when(authRepository.verificarUsuario(anyString(), anyString()))
            .thenReturn(null);
        
        LoginResponse response = service.authenticateUser(validRequest);
        
        assertThat(response).isNotNull();
        assertThat(response.getIdRol()).isNull();
        assertThat(response.getNombreUser()).isNull();
        assertThat(response.getMessage()).isEqualTo(Constantes.MSG_AUTH_FAILURE);
    }
    
    @Test
    @DisplayName("Debe retornar error cuando idRol es nulo en la respuesta")
    void debeRetornarErrorCuandoIdRolEsNuloEnLaRespuesta() {
        LoginResponse responseWithNullId = new LoginResponse(null, "User", "OK");
        
        when(authRepository.verificarUsuario(anyString(), anyString()))
            .thenReturn(responseWithNullId);
        
        LoginResponse response = service.authenticateUser(validRequest);
        
        assertThat(response).isNotNull();
        assertThat(response.getMessage()).isEqualTo(Constantes.MSG_AUTH_FAILURE);
    }
    
    @Test
    @DisplayName("Debe retornar error cuando message es nulo en la respuesta")
    void debeRetornarErrorCuandoMessageEsNuloEnLaRespuesta() {
        LoginResponse responseWithNullMessage = new LoginResponse("1", "User", null);
        
        when(authRepository.verificarUsuario(anyString(), anyString()))
            .thenReturn(responseWithNullMessage);
        
        LoginResponse response = service.authenticateUser(validRequest);
        
        assertThat(response).isNotNull();
        assertThat(response.getMessage()).isEqualTo(Constantes.MSG_AUTH_FAILURE);
    }
    
    @Test
    @DisplayName("Debe manejar excepción de base de datos")
    void debeManejarExcepcionDeBaseDatos() {
        when(authRepository.verificarUsuario(anyString(), anyString()))
            .thenThrow(new RuntimeException("Database connection error"));
        
        LoginResponse response = service.authenticateUser(validRequest);
        
        assertThat(response).isNotNull();
        assertThat(response.getIdRol()).isNull();
        assertThat(response.getNombreUser()).isNull();
        assertThat(response.getMessage()).isEqualTo(Constantes.DATABASE_ERROR);
    }
    
    @Test
    @DisplayName("Debe codificar el password antes de verificar")
    void debeCodificarElPasswordAntesDeVerificar() {
        when(authRepository.verificarUsuario(anyString(), anyString()))
            .thenReturn(successResponse);
        
        service.authenticateUser(validRequest);
        
        verify(authRepository).verificarUsuario(
            eq("admin"), 
            argThat(encodedPassword -> !encodedPassword.equals("password123"))
        );
    }
    
    @Test
    @DisplayName("Debe autenticar diferentes tipos de usuarios")
    void debeAutenticarDiferentesTiposDeUsuarios() {
        LoginRequest adminRequest = new LoginRequest(null, null, "admin", "adminpass");
        LoginRequest userRequest = new LoginRequest(null, null, "user", "userpass");
        
        LoginResponse adminResponse = new LoginResponse("1", "Admin", "OK");
        LoginResponse userResponse = new LoginResponse("2", "User", "OK");
        
        when(authRepository.verificarUsuario(eq("admin"), anyString()))
            .thenReturn(adminResponse);
        when(authRepository.verificarUsuario(eq("user"), anyString()))
            .thenReturn(userResponse);
        
        LoginResponse response1 = service.authenticateUser(adminRequest);
        LoginResponse response2 = service.authenticateUser(userRequest);
        
        assertThat(response1.getIdRol()).isEqualTo("1");
        assertThat(response2.getIdRol()).isEqualTo("2");
    }
}