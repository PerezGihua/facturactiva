package com.facturactiva.app.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.sql.Types;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Tests para SpParameter")
class SpParameterTest {

	@Test
    @DisplayName("Debe crear SpParameter vacío con constructor por defecto")
    void debeCrearSpParameterVacioConConstructorPorDefecto() {
        SpParameter param = new SpParameter();
        
        assertThat(param.getName()).isNull();
        assertThat(param.getValue()).isNull();
        assertThat(param.getSqlType()).isZero();
        assertThat(param.isOutput()).isFalse();
    }
    
    @Test
    @DisplayName("Debe crear SpParameter de entrada con constructor básico")
    void debeCrearSpParameterDeEntradaConConstructorBasico() {
        String name = "username";
        String value = "admin";
        int sqlType = Types.VARCHAR;
        
        SpParameter param = new SpParameter(name, value, sqlType);
        
        assertThat(param.getName()).isEqualTo(name);
        assertThat(param.getValue()).isEqualTo(value);
        assertThat(param.getSqlType()).isEqualTo(sqlType);
        assertThat(param.isOutput()).isFalse();
    }
    
    @Test
    @DisplayName("Debe crear SpParameter de salida con constructor output")
    void debeCrearSpParameterDeSalidaConConstructorOutput() {
        String name = "result";
        int sqlType = Types.INTEGER;
        
        SpParameter param = new SpParameter(name, sqlType);
        
        assertThat(param.getName()).isEqualTo(name);
        assertThat(param.getValue()).isNull();
        assertThat(param.getSqlType()).isEqualTo(sqlType);
        assertThat(param.isOutput()).isTrue();
    }
    
    @Test
    @DisplayName("Debe crear SpParameter completo con todos los parámetros")
    void debeCrearSpParameterCompletoConTodosLosParametros() {
        String name = "param";
        Object value = 123;
        int sqlType = Types.INTEGER;
        boolean output = true;
        
        SpParameter param = new SpParameter(name, value, sqlType, output);
        
        assertThat(param.getName()).isEqualTo(name);
        assertThat(param.getValue()).isEqualTo(value);
        assertThat(param.getSqlType()).isEqualTo(sqlType);
        assertThat(param.isOutput()).isTrue();
    }
    
    @Test
    @DisplayName("Debe crear parámetro de entrada usando método estático input")
    void debeCrearParametroDeEntradaUsandoMetodoEstaticoInput() {
        SpParameter param = SpParameter.input("username", "admin", Types.VARCHAR);
        
        assertThat(param.getName()).isEqualTo("username");
        assertThat(param.getValue()).isEqualTo("admin");
        assertThat(param.getSqlType()).isEqualTo(Types.VARCHAR);
        assertThat(param.isOutput()).isFalse();
    }
    
    @Test
    @DisplayName("Debe crear parámetro de salida usando método estático output")
    void debeCrearParametroDeSalidaUsandoMetodoEstaticoOutput() {
        SpParameter param = SpParameter.output("message", Types.VARCHAR);
        
        assertThat(param.getName()).isEqualTo("message");
        assertThat(param.getValue()).isNull();
        assertThat(param.getSqlType()).isEqualTo(Types.VARCHAR);
        assertThat(param.isOutput()).isTrue();
    }
    
    @Test
    @DisplayName("Debe crear parámetro de entrada/salida usando método estático inout")
    void debeCrearParametroInOutUsandoMetodoEstaticoInout() {
        SpParameter param = SpParameter.inout("counter", 0, Types.INTEGER);
        
        assertThat(param.getName()).isEqualTo("counter");
        assertThat(param.getValue()).isEqualTo(0);
        assertThat(param.getSqlType()).isEqualTo(Types.INTEGER);
        assertThat(param.isOutput()).isTrue();
    }
    
    @Test
    @DisplayName("Debe permitir modificar el nombre")
    void debePermitirModificarElNombre() {
        SpParameter param = new SpParameter();
        
        param.setName("newName");
        
        assertThat(param.getName()).isEqualTo("newName");
    }
    
    @Test
    @DisplayName("Debe permitir modificar el valor")
    void debePermitirModificarElValor() {
        SpParameter param = new SpParameter();
        
        param.setValue("newValue");
        
        assertThat(param.getValue()).isEqualTo("newValue");
    }
    
    @Test
    @DisplayName("Debe permitir modificar el tipo SQL")
    void debePermitirModificarElTipoSQL() {
        SpParameter param = new SpParameter();
        
        param.setSqlType(Types.VARCHAR);
        
        assertThat(param.getSqlType()).isEqualTo(Types.VARCHAR);
    }
    
    @Test
    @DisplayName("Debe permitir modificar la bandera output")
    void debePermitirModificarLaBanderaOutput() {
        SpParameter param = new SpParameter();
        
        param.setOutput(true);
        
        assertThat(param.isOutput()).isTrue();
    }
    
    @Test
    @DisplayName("Debe manejar valores de diferentes tipos")
    void debeManejarValoresDeDiferentesTipos() {
        SpParameter paramString = SpParameter.input("str", "text", Types.VARCHAR);
        SpParameter paramInt = SpParameter.input("num", 42, Types.INTEGER);
        SpParameter paramDouble = SpParameter.input("decimal", 3.14, Types.DOUBLE);
        SpParameter paramBool = SpParameter.input("flag", true, Types.BOOLEAN);
        
        assertThat(paramString.getValue()).isInstanceOf(String.class);
        assertThat(paramInt.getValue()).isInstanceOf(Integer.class);
        assertThat(paramDouble.getValue()).isInstanceOf(Double.class);
        assertThat(paramBool.getValue()).isInstanceOf(Boolean.class);
    }
}