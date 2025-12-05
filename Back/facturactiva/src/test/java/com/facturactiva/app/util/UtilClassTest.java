package com.facturactiva.app.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.sql.Types;
import java.util.Base64;
import java.util.Date;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Tests para UtilClass")
class UtilClassTest {

    @Test
    @DisplayName("Debe codificar string en Base64")
    void debeCodificarStringEnBase64() {
        String input = "password123";
        
        String encoded = UtilClass.encode(input);
        
        assertThat(encoded)
            .isNotNull()
            .isNotEmpty()
            .isNotEqualTo(input);
        
        // Verificar que es Base64 válido
        String decoded = new String(Base64.getDecoder().decode(encoded));
        assertThat(decoded).isEqualTo(input);
    }
    
    @Test
    @DisplayName("Debe retornar null cuando input es null")
    void debeRetornarNullCuandoInputEsNull() {
        String result = UtilClass.encode(null);
        assertThat(result).isNull();
    }
    
    @Test
    @DisplayName("Debe retornar string vacío cuando input es vacío")
    void debeRetornarStringVacioCuandoInputEsVacio() {
        String result = UtilClass.encode("");
        assertThat(result).isEmpty();
    }
    
    @Test
    @DisplayName("Debe codificar strings con caracteres especiales")
    void debeCodificarStringsConCaracteresEspeciales() {
        String input = "p@ssw0rd!#$%";
        
        String encoded = UtilClass.encode(input);
        String decoded = new String(Base64.getDecoder().decode(encoded));
        
        assertThat(decoded).isEqualTo(input);
    }
    
    @Test
    @DisplayName("Debe codificar strings largos")
    void debeCodificarStringsLargos() {
        String input = "a".repeat(1000);
        
        String encoded = UtilClass.encode(input);
        
        assertThat(encoded)
            .isNotNull()
            .isNotEmpty();
    }
    
    @Test
    @DisplayName("Debe retornar tipos SQL correctos para diferentes objetos")
    void debeRetornarTiposSqlCorrectosParaDiferentesObjetos() {
        assertThat(UtilClass.getSqlType("test")).isEqualTo(Types.VARCHAR);
        assertThat(UtilClass.getSqlType(123)).isEqualTo(Types.INTEGER);
        assertThat(UtilClass.getSqlType(123L)).isEqualTo(Types.BIGINT);
        assertThat(UtilClass.getSqlType(123.45)).isEqualTo(Types.DECIMAL);
        assertThat(UtilClass.getSqlType(123.45f)).isEqualTo(Types.DECIMAL);
        assertThat(UtilClass.getSqlType(true)).isEqualTo(Types.BOOLEAN);
        assertThat(UtilClass.getSqlType(new Date())).isEqualTo(Types.DATE);
        assertThat(UtilClass.getSqlType(new java.sql.Date(System.currentTimeMillis()))).isEqualTo(Types.DATE);
        assertThat(UtilClass.getSqlType(null)).isEqualTo(Types.NULL);
        
        Object unknownObject = new Object();
        assertThat(UtilClass.getSqlType(unknownObject)).isEqualTo(Types.VARCHAR);
    }
    
    @Test
    @DisplayName("Debe crear instancia de UtilClass")
    void debeCrearInstanciaDeUtilClass() {
        UtilClass utilClass = new UtilClass();
        assertThat(utilClass).isNotNull();
    }
}