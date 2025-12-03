package com.facturactiva.app.dto;

public class SpParameter {

	// CONFIGURACION DE PARAMETROS
	
	private String name;
    private Object value;
    private int sqlType;
    private boolean output;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public int getSqlType() {
		return sqlType;
	}

	public void setSqlType(int sqlType) {
		this.sqlType = sqlType;
	}

	public boolean isOutput() {
		return output;
	}

	public void setOutput(boolean output) {
		this.output = output;
	}
	
	// CONSTRUCTORS
	public SpParameter() {
    }

    public SpParameter(String name, Object value, int sqlType) {
        this.name = name;
        this.value = value;
        this.sqlType = sqlType;
        this.output = false;
    }

    public SpParameter(String name, int sqlType) {
        this.name = name;
        this.value = null;
        this.sqlType = sqlType;
        this.output = true;
    }

    public SpParameter(String name, Object value, int sqlType, boolean output) {
        this.name = name;
        this.value = value;
        this.sqlType = sqlType;
        this.output = output;
    }
	
    // CREAR PARAMETROS DE ENTRADA
    public static SpParameter input(String name, Object value, int sqlType) {
        return new SpParameter(name, value, sqlType, false);
    }

    public static SpParameter output(String name, int sqlType) {
        return new SpParameter(name, null, sqlType, true);
    }

    public static SpParameter inout(String name, Object value, int sqlType) {
        SpParameter param = new SpParameter(name, value, sqlType, true);
        return param;
    }
}
