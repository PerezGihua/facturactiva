package com.facturactiva.app.dto;

import java.util.List;

public class SpConfig {
	// CONFIGURACION COMPLETA SP
    private String procedureName;
    private List<SpParameter> parameters;
    
    public SpConfig(String procedureName) {
        this.procedureName = procedureName;
    }
    
    public SpConfig withParameters(List<SpParameter> parameters) {
        this.parameters = parameters;
        return this;
    }
    
    public String getProcedureName() { return procedureName; }
    public List<SpParameter> getParameters() { return parameters; }
}