package com.facturactiva.app.entity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Component;

import com.facturactiva.app.dto.SpConfig;
import com.facturactiva.app.dto.SpParameter;
import com.facturactiva.app.util.UtilClass;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class StoredProcedureExecutor {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
	 // EJECUTA UN STORED PROCEDURE CON PARÁMETROS DE ENTRADA Y SALIDA
	 // @PARAM PROCEDURENAME NOMBRE DEL STORED PROCEDURE
	 // @PARAM INPUTPARAMS MAPA CON LOS PARÁMETROS DE ENTRADA (NOMBRE, VALOR)
	 // @PARAM OUTPUTPARAMS MAPA CON LOS PARÁMETROS DE SALIDA (NOMBRE, TIPO SQL)
	 // @RETURN MAPA CON LOS RESULTADOS DE LOS PARÁMETROS DE SALIDA
    public Map<String, Object> execute(String procedureName, Map<String, Object> inputParams, Map<String, Integer> outputParams) {
        
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate).withProcedureName(procedureName);
        
        // Declarar parámetros de entrada
        if (inputParams != null && !inputParams.isEmpty()) {
            for (Map.Entry<String, Object> entry : inputParams.entrySet()) {
                jdbcCall.declareParameters(
                    new SqlParameter(entry.getKey(), UtilClass.getSqlType(entry.getValue()))
                );
            }
        }
        
        // Declarar parámetros de salida
        if (outputParams != null && !outputParams.isEmpty()) {
            for (Map.Entry<String, Integer> entry : outputParams.entrySet()) {
                jdbcCall.declareParameters(
                    new SqlOutParameter(entry.getKey(), entry.getValue())
                );
            }
        }
        
        // Preparar parámetros para ejecución
        Map<String, Object> params = new HashMap<>();
        if (inputParams != null) {
            params.putAll(inputParams);
        }
        if (outputParams != null) {
            for (String key : outputParams.keySet()) {
                params.put(key, null);
            }
        }
        
        return jdbcCall.execute(params);
    }
    
    // EJECUTA UN STORED PROCEDURE CON CONFIGURACIÓN PERSONALIZADA
    // @PARAM CONFIG CONFIGURACIÓN DEL STORED PROCEDURE
    // @RETURN MAPA CON LOS RESULTADOS
    public Map<String, Object> execute(SpConfig config) {
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
            .withProcedureName(config.getProcedureName());
        
        // Crear lista de parámetros para declarar todos juntos
        List<SqlParameter> sqlParameters = new ArrayList<>();
        
        if (config.getParameters() != null && !config.getParameters().isEmpty()) {
            for (SpParameter param : config.getParameters()) {
                if (param.isOutput()) {
                    sqlParameters.add(new SqlOutParameter(param.getName(), param.getSqlType()));
                } else {
                    sqlParameters.add(new SqlParameter(param.getName(), param.getSqlType()));
                }
            }
        }
        
        // Declarar todos los parámetros de una sola vez
        if (!sqlParameters.isEmpty()) {
            jdbcCall.declareParameters(sqlParameters.toArray(new SqlParameter[0]));
        }
        
        // Preparar valores de parámetros
        Map<String, Object> params = new HashMap<>();
        for (SpParameter param : config.getParameters()) {
            params.put(param.getName(), param.getValue());
        }
        
        return jdbcCall.execute(params);
    }
}