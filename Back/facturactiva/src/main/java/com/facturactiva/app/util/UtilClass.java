package com.facturactiva.app.util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public class UtilClass {
    
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
    
    public String getColumnIfExists(ResultSet rs, String columnName) {
        try {
            return rs.getString(columnName);
        } catch (SQLException e) {
            return null;
        }
    }

    public Integer getIntColumnIfExists(ResultSet rs, String columnName) {
        try {
            int value = rs.getInt(columnName);
            return rs.wasNull() ? null : value;
        } catch (SQLException e) {
            return null;
        }
    }

	public UtilClass() {
		super();
	}
}
