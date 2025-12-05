package com.facturactiva.app.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.sql.Types;
import java.util.Arrays;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Tests para SpConfig")
class SpConfigTest {

    @Test
    @DisplayName("Debe crear SpConfig con nombre de procedimiento")
    void debeCrearSpConfigConNombreDeProcedimiento() {
        String procedureName = "SP_Test";
        
        SpConfig config = new SpConfig(procedureName);
        
        assertThat(config.getProcedureName()).isEqualTo(procedureName);
        assertThat(config.getParameters()).isNull();
    }
    
    @Test
    @DisplayName("Debe agregar parámetros usando método fluido")
    void debeAgregarParametrosUsandoMetodoFluido() {
        List<SpParameter> parameters = Arrays.asList(
            SpParameter.input("param1", "value1", Types.VARCHAR),
            SpParameter.output("param2", Types.INTEGER)
        );
        
        SpConfig config = new SpConfig("SP_Test")
            .withParameters(parameters);
        
        assertThat(config.getParameters()).isNotNull();
        assertThat(config.getParameters()).hasSize(2);
        assertThat(config.getParameters()).isEqualTo(parameters);
    }
    
    @Test
    @DisplayName("Debe retornar el mismo objeto al usar withParameters")
    void debeRetornarElMismoObjetoAlUsarWithParameters() {
        SpConfig config = new SpConfig("SP_Test");
        List<SpParameter> parameters = Arrays.asList(
            SpParameter.input("test", "value", Types.VARCHAR)
        );
        
        SpConfig result = config.withParameters(parameters);
        
        assertThat(result).isSameAs(config);
    }
    
    @Test
    @DisplayName("Debe crear SpConfig sin parámetros")
    void debeCrearSpConfigSinParametros() {
        SpConfig config = new SpConfig("SP_Simple");
        
        assertThat(config.getProcedureName()).isEqualTo("SP_Simple");
        assertThat(config.getParameters()).isNull();
    }
    
    @Test
    @DisplayName("Debe aceptar lista vacía de parámetros")
    void debeAceptarListaVaciaDeParametros() {
        SpConfig config = new SpConfig("SP_Test")
            .withParameters(Arrays.asList());
        
        assertThat(config.getParameters()).isEmpty();
    }
}