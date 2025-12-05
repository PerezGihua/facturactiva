package com.facturactiva.app.entity;

import com.facturactiva.app.dto.LoginResponse;
import com.facturactiva.app.dto.SpConfig;
import com.facturactiva.app.util.Constantes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests para LoginEntity")
class LoginEntityTest {

    @Mock
    private StoredProcedureExecutor spExecutor;
    
    @InjectMocks
    private LoginEntity loginEntity;
    
    private static final String RESULT_SET_KEY = "#result-set-1";
    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_PASSWORD = "testpass";
    
    @Test
    @DisplayName("Debe verificar usuario exitosamente cuando el SP retorna datos válidos")
    void debeVerificarUsuarioExitosamente() {
        // Arrange
        Map<String, Object> spResult = new HashMap<>();
        Map<String, Object> resultRow = new HashMap<>();
        resultRow.put(Constantes.PARAM_ID_ROL, 1);
        resultRow.put(Constantes.PARAM_MESSAGE, Constantes.MSG_AUTH_SUCCESS);
        resultRow.put(Constantes.PARAM_NOMBRE_USER, "Test User");
        
        List<Map<String, Object>> resultSet = Arrays.asList(resultRow);
        spResult.put(RESULT_SET_KEY, resultSet);
        
        when(spExecutor.execute(any(SpConfig.class))).thenReturn(spResult);
        
        // Act
        LoginResponse response = loginEntity.verificarUsuario(TEST_USERNAME, TEST_PASSWORD);
        
        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getIdRol()).isEqualTo("1");
        assertThat(response.getNombreUser()).isEqualTo("Test User");
        assertThat(response.getMessage()).isEqualTo(Constantes.MSG_AUTH_SUCCESS);
    }
    
    @Test
    @DisplayName("Debe retornar falla de autenticación cuando el SP retorna resultado vacío")
    void debeRetornarFallaCuandoSpRetornaResultadoVacio() {
        // Arrange
        Map<String, Object> spResult = new HashMap<>();
        spResult.put(RESULT_SET_KEY, Arrays.asList());
        
        when(spExecutor.execute(any(SpConfig.class))).thenReturn(spResult);
        
        // Act
        LoginResponse response = loginEntity.verificarUsuario(TEST_USERNAME, TEST_PASSWORD);
        
        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getIdRol()).isNull();
        assertThat(response.getNombreUser()).isNull();
        assertThat(response.getMessage()).isEqualTo(Constantes.MSG_AUTH_FAILURE);
    }
    
    @Test
    @DisplayName("Debe retornar falla de autenticación cuando el resultado es null")
    void debeRetornarFallaCuandoResultadoEsNull() {
        // Arrange
        Map<String, Object> spResult = new HashMap<>();
        spResult.put(RESULT_SET_KEY, null);
        
        when(spExecutor.execute(any(SpConfig.class))).thenReturn(spResult);
        
        // Act
        LoginResponse response = loginEntity.verificarUsuario(TEST_USERNAME, TEST_PASSWORD);
        
        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getIdRol()).isNull();
        assertThat(response.getNombreUser()).isNull();
        assertThat(response.getMessage()).isEqualTo(Constantes.MSG_AUTH_FAILURE);
    }
    
    @Test
    @DisplayName("Debe manejar null en idRol del resultado")
    void debeManejarNullEnIdRol() {
        // Arrange
        Map<String, Object> spResult = new HashMap<>();
        Map<String, Object> resultRow = new HashMap<>();
        resultRow.put(Constantes.PARAM_ID_ROL, null);
        resultRow.put(Constantes.PARAM_MESSAGE, "Custom Message");
        resultRow.put(Constantes.PARAM_NOMBRE_USER, "Test User");
        
        List<Map<String, Object>> resultSet = Arrays.asList(resultRow);
        spResult.put(RESULT_SET_KEY, resultSet);
        
        when(spExecutor.execute(any(SpConfig.class))).thenReturn(spResult);
        
        // Act
        LoginResponse response = loginEntity.verificarUsuario(TEST_USERNAME, TEST_PASSWORD);
        
        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getIdRol()).isNull();
        assertThat(response.getNombreUser()).isEqualTo("Test User");
        assertThat(response.getMessage()).isEqualTo("Custom Message");
    }
    
    @Test
    @DisplayName("Debe configurar parámetros correctamente para el SP")
    void debeConfigurarParametrosCorrectamente() {
        // Arrange
        Map<String, Object> spResult = new HashMap<>();
        Map<String, Object> resultRow = new HashMap<>();
        resultRow.put(Constantes.PARAM_ID_ROL, 2);
        resultRow.put(Constantes.PARAM_MESSAGE, Constantes.MSG_AUTH_SUCCESS);
        resultRow.put(Constantes.PARAM_NOMBRE_USER, "Admin User");
        
        List<Map<String, Object>> resultSet = Arrays.asList(resultRow);
        spResult.put(RESULT_SET_KEY, resultSet);
        
        when(spExecutor.execute(any(SpConfig.class))).thenReturn(spResult);
        
        // Act
        LoginResponse response = loginEntity.verificarUsuario("admin", "admin123");
        
        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getIdRol()).isEqualTo("2");
        assertThat(response.getNombreUser()).isEqualTo("Admin User");
        assertThat(response.getMessage()).isEqualTo(Constantes.MSG_AUTH_SUCCESS);
    }
}