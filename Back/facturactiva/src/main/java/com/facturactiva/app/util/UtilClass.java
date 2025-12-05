package com.facturactiva.app.util;

import java.sql.Types;
import java.util.Base64;

public class UtilClass {

	// CODIFICA EN BASE64
    public static String encode(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return Base64.getEncoder().encodeToString(input.getBytes());
    }
    
    // DETERMINA EL TIPO SQL BASADO EN EL TIPO DE OBJETO JAVA
    public static int getSqlType(Object value) {
        if (value == null) return Types.NULL;
        if (value instanceof String) return Types.VARCHAR;
        if (value instanceof Integer) return Types.INTEGER;
        if (value instanceof Long) return Types.BIGINT;
        if (value instanceof Double || value instanceof Float) return Types.DECIMAL;
        if (value instanceof Boolean) return Types.BOOLEAN;
        if (value instanceof java.util.Date || value instanceof java.sql.Date) return Types.DATE;
        return Types.VARCHAR;
    }

	public UtilClass() {
		super();
	}
}
