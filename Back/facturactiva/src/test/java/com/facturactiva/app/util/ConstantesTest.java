package com.facturactiva.app.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Tests para Constantes")
class ConstantesTest {

    @Test
    @DisplayName("Las URLs deben estar definidas correctamente")
    void lasUrlsDebenEstarDefinidasCorrectamente() {
        assertThat(Constantes.FRONT_URL)
            .isNotNull()
            .isEqualTo("http://localhost:4200");
        
        assertThat(Constantes.RUTA_LOGS)
            .isNotNull()
            .isEqualTo("C:/facturactiva/logs");
    }
    
    @Test
    @DisplayName("Los parámetros del SP deben estar definidos")
    void losParametrosDelSpDebenEstarDefinidos() {
        assertThat(Constantes.PARAM_USERNAME)
            .describedAs("PARAM_USERNAME")
            .isEqualTo("username");
        assertThat(Constantes.PARAM_PASSWORD)
            .describedAs("PARAM_PASSWORD")
            .isEqualTo("psw");
        assertThat(Constantes.PARAM_ID_ROL)
            .describedAs("PARAM_ID_ROL")
            .isEqualTo("id_rol");
        assertThat(Constantes.PARAM_MESSAGE)
            .describedAs("PARAM_MESSAGE")
            .isEqualTo("message");
        assertThat(Constantes.PARAM_NOMBRE_USER)
            .describedAs("PARAM_NOMBRE_USER")
            .isEqualTo("nombreUser");
    }
    
    @Test
    @DisplayName("Los mensajes de autenticación deben estar definidos")
    void losMensajesDeAutenticacionDebenEstarDefinidos() {
        assertThat(Constantes.MSG_AUTH_SUCCESS)
            .describedAs("MSG_AUTH_SUCCESS")
            .isNotNull();
        assertThat(Constantes.MSG_AUTH_FAILURE)
            .describedAs("MSG_AUTH_FAILURE")
            .isNotNull();
        assertThat(Constantes.MSG_USER_NOT_FOUND)
            .describedAs("MSG_USER_NOT_FOUND")
            .isNotNull();
    }
    
    @Test
    @DisplayName("Los nombres de tablas deben estar definidos")
    void losNombresDeTablaDebenEstarDefinidos() {
        assertThat(Constantes.TABLE_USERS)
            .isEqualTo("Users");
        assertThat(Constantes.TABLE_ROLES)
            .isEqualTo("Roles");
    }
    
    @Test
    @DisplayName("Los nombres de columnas deben estar definidos")
    void losNombresDeColumnasDebenEstarDefinidos() {
        assertThat(Constantes.COLUMN_USER_ID)
            .describedAs("COLUMN_USER_ID")
            .isNotNull();
        assertThat(Constantes.COLUMN_USER_NAME)
            .describedAs("COLUMN_USER_NAME")
            .isNotNull();
        assertThat(Constantes.COLUMN_PASSWORD)
            .describedAs("COLUMN_PASSWORD")
            .isNotNull();
        assertThat(Constantes.COLUMN_ROLE_ID)
            .describedAs("COLUMN_ROLE_ID")
            .isNotNull();
    }
    
    @Test
    @DisplayName("Los códigos de error deben estar definidos")
    void losCodigosDeErrorDebenEstarDefinidos() {
        assertThat(Constantes.DATABASE_ERROR)
            .describedAs("DATABASE_ERROR")
            .isEqualTo("ERROR_DB");
        assertThat(Constantes.INVALID_CREDENTIALS)
            .describedAs("INVALID_CREDENTIALS")
            .isEqualTo("INVALID_CREDENTIALS");
        assertThat(Constantes.UNKNOWN_ERROR)
            .describedAs("UNKNOWN_ERROR")
            .isEqualTo("UNKNOWN_ERROR");
        assertThat(Constantes.INVALID_INPUT)
            .describedAs("INVALID_INPUT")
            .isEqualTo("ERROR_INVALID_INPUT");
    }
    
    @Test
    @DisplayName("Los métodos HTTP deben estar definidos")
    void losMetodosHttpDebenEstarDefinidos() {
        assertThat(Constantes.GET)
            .isEqualTo("GET");
        assertThat(Constantes.POST)
            .isEqualTo("POST");
        assertThat(Constantes.PUT)
            .isEqualTo("PUT");
        assertThat(Constantes.DELETE)
            .isEqualTo("DELETE");
    }
    
    @Test
    @DisplayName("Los caracteres especiales deben estar definidos")
    void losCaracteresEspecialesDebenEstarDefinidos() {
        assertThat(Constantes.ASTERISCO)
            .isEqualTo("*");
        assertThat(Constantes.UNO)
            .isEqualTo("1");
        assertThat(Constantes.ESPACIO_BLANCO)
            .isEqualTo(" ");
    }
    
    @Test
    @DisplayName("Todas las constantes deben ser no nulas")
    void todasLasConstantesDebenSerNoNulas() {
        assertThat(Constantes.FRONT_URL)
            .isNotNull();
        assertThat(Constantes.RUTA_LOGS)
            .isNotNull();
        assertThat(Constantes.PARAM_USERNAME)
            .isNotNull();
        assertThat(Constantes.MSG_AUTH_SUCCESS)
            .isNotNull();
        assertThat(Constantes.SP_VALIDAR_USER)
            .isNotNull();
        assertThat(Constantes.DATABASE_ERROR)
            .isNotNull();
    }
    
    @Test
    @DisplayName("Propiedades relacionadas con SP deben estar definidas")
    void propiedadesSpDebenEstarDefinidas() {
        assertThat(Constantes.METODO_AUTHENTICATE_USER)
            .describedAs("METODO_AUTHENTICATE_USER")
            .isNotNull();
        assertThat(Constantes.SP_VALIDAR_USER)
            .describedAs("SP_VALIDAR_USER")
            .isEqualTo("SP_Validar_User");
        assertThat(Constantes.RESULT_SET_KEY)
            .describedAs("RESULT_SET_KEY")
            .isEqualTo("#result-set-1");
    }
    
    // Tests adicionales para evitar que haya llamadas a describedAs() sin aserciones
    @Test
    @DisplayName("Los nombres de métodos deben estar definidos")
    void losNombresDeMetodosDebenEstarDefinidos() {
        assertThat(Constantes.METODO_AUTHENTICATE_USER)
            .isNotNull()
            .contains("AUTENTICACION");
    }
    
    @Test
    @DisplayName("Los nombres de SP deben estar definidos")
    void losNombresDeSpDebenEstarDefinidos() {
        assertThat(Constantes.SP_VALIDAR_USER)
            .isEqualTo("SP_Validar_User");
    }
    
    @Test
    @DisplayName("Las keys de resultados deben estar definidas")
    void lasKeysDeResultadosDebenEstarDefinidas() {
        assertThat(Constantes.RESULT_SET_KEY)
            .isEqualTo("#result-set-1");
    }
}