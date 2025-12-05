package com.facturactiva.app.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Tests para LoginRequest")
class LoginRequestTest {

    @Test
    @DisplayName("Debe crear LoginRequest con todos los valores")
    void debeCrearLoginRequestConTodosLosValores() {
        String codRpta = "200";
        String msgRpta = "OK";
        String username = "admin";
        String password = "password123";
        
        LoginRequest request = new LoginRequest(codRpta, msgRpta, username, password);
        
        assertThat(request.getCodRpta()).isEqualTo(codRpta);
        assertThat(request.getMsgRpta()).isEqualTo(msgRpta);
        assertThat(request.getUsername()).isEqualTo(username);
        assertThat(request.getPassword()).isEqualTo(password);
    }
    
    @Test
    @DisplayName("Debe heredar propiedades de BaseEntity")
    void debeHeredarPropiedadesDeBaseEntity() {
        LoginRequest request = new LoginRequest("200", "OK", "user", "pass");
        
        assertThat(request).isInstanceOf(BaseEntity.class);
        assertThat(request.getCodRpta()).isNotNull();
        assertThat(request.getMsgRpta()).isNotNull();
    }
    
    @Test
    @DisplayName("Debe permitir modificar username")
    void debePermitirModificarUsername() {
        LoginRequest request = new LoginRequest(null, null, "admin", "pass");
        
        request.setUsername("newuser");
        
        assertThat(request.getUsername()).isEqualTo("newuser");
    }
    
    @Test
    @DisplayName("Debe permitir modificar password")
    void debePermitirModificarPassword() {
        LoginRequest request = new LoginRequest(null, null, "admin", "pass");
        
        request.setPassword("newpassword");
        
        assertThat(request.getPassword()).isEqualTo("newpassword");
    }
    
    @Test
    @DisplayName("Debe aceptar username y password nulos")
    void debeAceptarUsernameYPasswordNulos() {
        LoginRequest request = new LoginRequest(null, null, null, null);
        
        assertThat(request.getUsername()).isNull();
        assertThat(request.getPassword()).isNull();
    }
    
    @Test
    @DisplayName("Debe aceptar username y password vacíos")
    void debeAceptarUsernameYPasswordVacios() {
        LoginRequest request = new LoginRequest(null, null, "", "");
        
        assertThat(request.getUsername()).isEmpty();
        assertThat(request.getPassword()).isEmpty();
    }
}