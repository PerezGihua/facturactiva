package com.facturactiva.app.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Tests para AuthException")
class AuthExceptionTest {

    @Test
    @DisplayName("Debe crear AuthException con mensaje")
    void debeCrearAuthExceptionConMensaje() {
        String message = "Error de autenticación";
        
        AuthException exception = new AuthException(message);
        
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }
    
    @Test
    @DisplayName("Debe crear AuthException con mensaje y causa")
    void debeCrearAuthExceptionConMensajeYCausa() {
        String message = "Error de autenticación";
        Throwable cause = new IllegalArgumentException("Argumento inválido");
        
        AuthException exception = new AuthException(message, cause);
        
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.getCause().getMessage()).isEqualTo("Argumento inválido");
    }
    
    @Test
    @DisplayName("Debe ser lanzada correctamente")
    void debeSerLanzadaCorrectamente() {
        assertThatThrownBy(() -> {
            throw new AuthException("Error de prueba");
        })
        .isInstanceOf(AuthException.class)
        .hasMessage("Error de prueba");
    }
    
    @Test
    @DisplayName("Debe ser lanzada con causa correctamente")
    void debeSerLanzadaConCausaCorrectamente() {
        Throwable cause = new RuntimeException("Causa original");
        
        assertThatThrownBy(() -> {
            throw new AuthException("Error wrapper", cause);
        })
        .isInstanceOf(AuthException.class)
        .hasMessage("Error wrapper")
        .hasCause(cause);
    }
    
    @Test
    @DisplayName("Debe heredar de RuntimeException")
    void debeHeredarDeRuntimeException() {
        AuthException exception = new AuthException("Test");
        
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }
    
    @Test
    @DisplayName("Debe aceptar mensaje nulo")
    void debeAceptarMensajeNulo() {
        AuthException exception = new AuthException(null);
        
        assertThat(exception.getMessage()).isNull();
    }
    
    @Test
    @DisplayName("Debe preservar el stack trace")
    void debePreservarElStackTrace() {
        AuthException exception = new AuthException("Error con stack trace");
        
        assertThat(exception.getStackTrace()).isNotNull();
        assertThat(exception.getStackTrace()).isNotEmpty();
    }
}