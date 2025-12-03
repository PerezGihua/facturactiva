package com.facturactiva.app.entity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.facturactiva.app.dto.LoginResponse;
import com.facturactiva.app.dto.SpConfig;
import com.facturactiva.app.dto.SpParameter;
import com.facturactiva.app.util.Constantes;
import java.sql.Types;
import java.util.*;

@Repository
public class LoginEntity {
    
    @Autowired
    private StoredProcedureExecutor spExecutor;
    
    public LoginResponse verificarUsuario(String username, String password) {
    	List<SpParameter> parameters = Arrays.asList(
			SpParameter.input(Constantes.PARAM_USERNAME, username, Types.VARCHAR),
            SpParameter.input(Constantes.PARAM_PASSWORD, password, Types.VARCHAR),
            SpParameter.output(Constantes.PARAM_ID_ROL, Types.VARCHAR),
            SpParameter.output(Constantes.PARAM_NOMBRE_USER, Types.VARCHAR),
            SpParameter.output(Constantes.PARAM_APELLIDO_USER, Types.VARCHAR),
            SpParameter.output(Constantes.PARAM_MESSAGE, Types.VARCHAR)
        );
        
    	SpConfig config = new SpConfig(Constantes.SP_VALIDAR_USER).withParameters(parameters);
        
        Map<String, Object> result = spExecutor.execute(config);
        
        String roleName = (String) result.get(Constantes.PARAM_ID_ROL);
        String nombreUser = (String) result.get(Constantes.PARAM_NOMBRE_USER) + Constantes.ESPACIO_BLANCO + (String) result.get(Constantes.PARAM_APELLIDO_USER);
        String message = (String) result.get(Constantes.PARAM_MESSAGE);
        
        return new LoginResponse(roleName,nombreUser ,message);
    }
}