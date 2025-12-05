package com.facturactiva.app.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Tests para BaseEntity")
class BaseEntityTest {

    @Test
    @DisplayName("Debe crear BaseEntity con valores correctos")
    void debeCrearBaseEntityConValoresCorrectos() {
        String codRpta = "200";
        String msgRpta = "Operación exitosa";
        
        BaseEntity entity = new BaseEntity(codRpta, msgRpta);
        
        assertThat(entity.getCodRpta()).isEqualTo(codRpta);
        assertThat(entity.getMsgRpta()).isEqualTo(msgRpta);
    }
    
    @Test
    @DisplayName("Debe permitir modificar codRpta")
    void debePermitirModificarCodRpta() {
        BaseEntity entity = new BaseEntity("200", "OK");
        
        entity.setCodRpta("500");
        
        assertThat(entity.getCodRpta()).isEqualTo("500");
    }
    
    @Test
    @DisplayName("Debe permitir modificar msgRpta")
    void debePermitirModificarMsgRpta() {
        BaseEntity entity = new BaseEntity("200", "OK");
        
        entity.setMsgRpta("Error interno");
        
        assertThat(entity.getMsgRpta()).isEqualTo("Error interno");
    }
    
    @Test
    @DisplayName("Debe aceptar valores nulos en constructor")
    void debeAceptarValoresNulosEnConstructor() {
        BaseEntity entity = new BaseEntity(null, null);
        
        assertThat(entity.getCodRpta()).isNull();
        assertThat(entity.getMsgRpta()).isNull();
    }
}