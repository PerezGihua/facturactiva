package com.facturactiva.app.repository;

import com.facturactiva.app.dto.RegisterRequest;
import com.facturactiva.app.entity.UsuarioEntity;
import com.facturactiva.app.util.Constantes;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Log4j2
public class AuthRepository {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Valida la existencia y estado del usuario mediante SP
     */
    public Map<String, Object> validateUser(String email, String password) {
        log.debug("{} Validando usuario: {}", Constantes.METODO_AUTHENTICATE_USER, email);

        try {
            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withProcedureName(Constantes.SP_VALIDAR_USER)
                    .declareParameters(
                            new SqlParameter(Constantes.PARAM_USERNAME, Types.VARCHAR),
                            new SqlParameter(Constantes.PARAM_PASSWORD, Types.VARCHAR)
                    )
                    .returningResultSet(Constantes.RESULT_SET_KEY, (rs, rowNum) -> {
                        return Map.of(
                                Constantes.PARAM_ID_ROL, rs.getObject(Constantes.PARAM_ID_ROL),
                                Constantes.PARAM_MESSAGE, rs.getString(Constantes.PARAM_MESSAGE),
                                Constantes.PARAM_NOMBRE_USER, rs.getString(Constantes.PARAM_NOMBRE_USER)
                        );
                    });

            Map<String, Object> params = Map.of(
                    Constantes.PARAM_USERNAME, email,
                    Constantes.PARAM_PASSWORD, password != null ? password : ""
            );

            Map<String, Object> result = jdbcCall.execute(params);
            
            @SuppressWarnings("unchecked")
            var resultList = (java.util.List<Map<String, Object>>) result.get(Constantes.RESULT_SET_KEY);

            if (resultList != null && !resultList.isEmpty()) {
                return resultList.get(0);
            }

            return Map.of(
                    Constantes.PARAM_ID_ROL, null,
                    Constantes.PARAM_MESSAGE, Constantes.MSG_AUTH_FAILURE,
                    Constantes.PARAM_NOMBRE_USER, null
            );

        } catch (Exception e) {
            log.error("{} Error al validar usuario: {}", Constantes.METODO_AUTHENTICATE_USER, e.getMessage(), e);
            throw new RuntimeException(Constantes.DATABASE_ERROR, e);
        }
    }

    /**
     * Obtiene un usuario por email usando Stored Procedure
     */
    public Optional<UsuarioEntity> findByEmail(String email) {
        log.debug("Buscando usuario por email mediante SP: {}", email);

        try {
            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withProcedureName("SP_ObtenerUsuarioPorEmail")
                    .declareParameters(
                            new SqlParameter("p_email", Types.VARCHAR)
                    )
                    .returningResultSet("result", new UsuarioRowMapper());

            Map<String, Object> params = Map.of("p_email", email);
            Map<String, Object> result = jdbcCall.execute(params);

            @SuppressWarnings("unchecked")
            var resultList = (java.util.List<UsuarioEntity>) result.get("result");

            if (resultList != null && !resultList.isEmpty()) {
                log.debug("Usuario encontrado: {}", email);
                return Optional.of(resultList.get(0));
            }

            log.debug("Usuario no encontrado: {}", email);
            return Optional.empty();

        } catch (Exception e) {
            log.error("Error al buscar usuario por email mediante SP: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * Registra un nuevo usuario usando Stored Procedure
     */
    public Map<String, Object> registerUser(RegisterRequest request, String passwordHash) {
        log.debug("{} Registrando usuario: {}", Constantes.METODO_REGISTER, request.getEmail());

        try {
            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withProcedureName(Constantes.SP_REGISTRAR_USUARIO)
                    .declareParameters(
                            new SqlParameter(Constantes.PARAM_P_ID_ROL, Types.INTEGER),
                            new SqlParameter(Constantes.PARAM_P_EMAIL, Types.VARCHAR),
                            new SqlParameter(Constantes.PARAM_P_PASSWORD_HASH, Types.VARCHAR),
                            new SqlParameter(Constantes.PARAM_P_NOMBRES, Types.VARCHAR),
                            new SqlParameter(Constantes.PARAM_P_APELLIDOS, Types.VARCHAR)
                    )
                    .returningResultSet(Constantes.RESULT_SET_KEY, (rs, rowNum) -> {
                        return Map.of(
                                Constantes.PARAM_ID_USUARIO, rs.getObject(Constantes.PARAM_ID_USUARIO),
                                Constantes.PARAM_MESSAGE, rs.getString(Constantes.PARAM_MESSAGE),
                                Constantes.PARAM_ERROR_CODE, rs.getInt(Constantes.PARAM_ERROR_CODE)
                        );
                    });

            Map<String, Object> params = Map.of(
                    Constantes.PARAM_P_ID_ROL, request.getIdRol(),
                    Constantes.PARAM_P_EMAIL, request.getEmail(),
                    Constantes.PARAM_P_PASSWORD_HASH, passwordHash,
                    Constantes.PARAM_P_NOMBRES, request.getNombres(),
                    Constantes.PARAM_P_APELLIDOS, request.getApellidos() != null ? request.getApellidos() : ""
            );

            Map<String, Object> result = jdbcCall.execute(params);
            
            @SuppressWarnings("unchecked")
            var resultList = (java.util.List<Map<String, Object>>) result.get(Constantes.RESULT_SET_KEY);

            if (resultList != null && !resultList.isEmpty()) {
                log.info("{} Usuario registrado exitosamente: {}", Constantes.METODO_REGISTER, request.getEmail());
                return resultList.get(0);
            }

            log.error("{} Error al registrar usuario: no se obtuvo respuesta del SP", Constantes.METODO_REGISTER);
            return Map.of(
                    Constantes.PARAM_ID_USUARIO, null,
                    Constantes.PARAM_MESSAGE, Constantes.MSG_REGISTER_FAILURE,
                    Constantes.PARAM_ERROR_CODE, -1
            );

        } catch (Exception e) {
            log.error("{} Error al registrar usuario: {}", Constantes.METODO_REGISTER, e.getMessage(), e);
            return Map.of(
                    Constantes.PARAM_ID_USUARIO, null,
                    Constantes.PARAM_MESSAGE, e.getMessage(),
                    Constantes.PARAM_ERROR_CODE, -1
            );
        }
    }

    /**
     * Verifica si un email ya existe en la base de datos
     */
    public boolean emailExists(String email) {
        log.debug("Verificando si el email existe: {}", email);

        try {
            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withProcedureName(Constantes.SP_VERIFICAR_EMAIL_EXISTE)
                    .declareParameters(
                            new SqlParameter(Constantes.PARAM_P_EMAIL, Types.VARCHAR)
                    )
                    .returningResultSet(Constantes.RESULT_SET_KEY, (rs, rowNum) -> {
                        return rs.getBoolean("existe");
                    });

            Map<String, Object> params = Map.of(Constantes.PARAM_P_EMAIL, email);
            Map<String, Object> result = jdbcCall.execute(params);

            @SuppressWarnings("unchecked")
            var resultList = (java.util.List<Boolean>) result.get(Constantes.RESULT_SET_KEY);

            if (resultList != null && !resultList.isEmpty()) {
                return resultList.get(0);
            }

            return false;

        } catch (Exception e) {
            log.error("Error al verificar si el email existe: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * RowMapper para mapear los resultados del SP a UsuarioEntity
     */
    private static class UsuarioRowMapper implements RowMapper<UsuarioEntity> {
        @Override
        public UsuarioEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
            return UsuarioEntity.builder()
                    .idUsuario(rs.getInt("id_usuario"))
                    .idRol(rs.getInt("id_rol"))
                    .email(rs.getString("email"))
                    .passwordHash(rs.getString("password_hash"))
                    .nombres(rs.getString("nombres"))
                    .apellidos(rs.getString("apellidos"))
                    .fechaRegistro(rs.getObject("fecha_registro", java.time.OffsetDateTime.class))
                    .activo(rs.getBoolean("activo"))
                    .build();
        }
    }
}