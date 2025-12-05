package com.facturactiva.app.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.facturactiva.app.util.Constantes;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Tests para LoginResponse")
class LoginResponseTest {

    @Test
    @DisplayName("Debe crear LoginResponse con todos los valores")
    void debeCrearLoginResponseConTodosLosValores() {
        String idRol = "1";
        String nombreUser = "Admin User";
        String message = Constantes.MSG_AUTH_SUCCESS;
        
        LoginResponse response = new LoginResponse(idRol, nombreUser, message);
        
        assertThat(response.getIdRol()).isEqualTo(idRol);
        assertThat(response.getNombreUser()).isEqualTo(nombreUser);
        assertThat(response.getMessage()).isEqualTo(message);
    }
    
    @Test
    @DisplayName("Debe permitir modificar idRol")
    void debePermitirModificarIdRol() {
        LoginResponse response = new LoginResponse("1", "User", "OK");
        
        response.setIdRol("2");
        
        assertThat(response.getIdRol()).isEqualTo("2");
    }
    
    @Test
    @DisplayName("Debe permitir modificar nombreUser")
    void debePermitirModificarNombreUser() {
        LoginResponse response = new LoginResponse("1", "User", "OK");
        
        response.setNombreUser("New User");
        
        assertThat(response.getNombreUser()).isEqualTo("New User");
    }
    
    @Test
    @DisplayName("Debe permitir modificar message")
    void debePermitirModificarMessage() {
        LoginResponse response = new LoginResponse("1", "User", "OK");
        
        response.setMessage(Constantes.MSG_AUTH_FAILURE);
        
        assertThat(response.getMessage()).isEqualTo(Constantes.MSG_AUTH_FAILURE);
    }
    
    @Test
    @DisplayName("Debe crear LoginResponse con autenticación exitosa")
    void debeCrearLoginResponseConAutenticacionExitosa() {
        LoginResponse response = new LoginResponse("1", "Admin", Constantes.MSG_AUTH_SUCCESS);
        
        assertThat(response.getIdRol()).isNotNull();
        assertThat(response.getNombreUser()).isNotNull();
        assertThat(response.getMessage()).isEqualTo(Constantes.MSG_AUTH_SUCCESS);
    }
    
    @Test
    @DisplayName("Debe crear LoginResponse con autenticación fallida")
    void debeCrearLoginResponseConAutenticacionFallida() {
        LoginResponse response = new LoginResponse(null, null, Constantes.MSG_AUTH_FAILURE);
        
        assertThat(response.getIdRol()).isNull();
        assertThat(response.getNombreUser()).isNull();
        assertThat(response.getMessage()).isEqualTo(Constantes.MSG_AUTH_FAILURE);
    }
    
    @Test
    @DisplayName("Debe aceptar valores nulos")
    void debeAceptarValoresNulos() {
        LoginResponse response = new LoginResponse(null, null, null);
        
        assertThat(response.getIdRol()).isNull();
        assertThat(response.getNombreUser()).isNull();
        assertThat(response.getMessage()).isNull();
    }
}