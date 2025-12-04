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
    
    private static final String RESULT_SET_KEY = "#result-set-1";
    
    public LoginResponse verificarUsuario(String username, String password) {
        List<SpParameter> parameters = Arrays.asList(
            SpParameter.input(Constantes.PARAM_USERNAME, username, Types.VARCHAR),
            SpParameter.input(Constantes.PARAM_PASSWORD, password, Types.VARCHAR),
            SpParameter.output(Constantes.PARAM_ID_ROL, Types.INTEGER),
            SpParameter.output(Constantes.PARAM_MESSAGE, Types.VARCHAR),
            SpParameter.output(Constantes.PARAM_NOMBRE_USER, Types.VARCHAR)
        );
        
        SpConfig config = new SpConfig(Constantes.SP_VALIDAR_USER)
            .withParameters(parameters);
        
        Map<String, Object> result = spExecutor.execute(config);
        
        System.out.println("Resultado completo: " + result);
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> resultSet = (List<Map<String, Object>>) result.get(RESULT_SET_KEY);
        
        if (resultSet != null && !resultSet.isEmpty()) {
            Map<String, Object> row = resultSet.get(0);
            
            Integer idRol = (Integer) row.get(Constantes.PARAM_ID_ROL);
            String message = (String) row.get(Constantes.PARAM_MESSAGE);
            String nombreUser = (String) row.get(Constantes.PARAM_NOMBRE_USER);
            
            return new LoginResponse(idRol != null ? String.valueOf(idRol) : null, nombreUser, message);
        }
        
        return new LoginResponse(null, null, Constantes.MSG_AUTH_FAILURE);
    }
}