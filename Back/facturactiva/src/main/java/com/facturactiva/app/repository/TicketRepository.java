package com.facturactiva.app.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Repository;

import com.facturactiva.app.dto.TicketDTO;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class TicketRepository {
    
    private final JdbcTemplate jdbcTemplate;
    
    private final RowMapper<TicketDTO> ticketRowMapper = new RowMapper<TicketDTO>() {
        @Override
        public TicketDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
            return TicketDTO.builder()
                    .idTicket(rs.getInt("id_ticket"))
                    .asunto(rs.getString("asunto"))
                    .descripcion(rs.getString("descripcion"))
                    .numeroDocumentoRechazado(rs.getString("numero_documento_rechazado"))
                    .fechaCreacion(rs.getTimestamp("fecha_creacion") != null ? 
                        rs.getTimestamp("fecha_creacion").toLocalDateTime() : null)
                    .fechaUltimaActualizacion(rs.getTimestamp("fecha_ultima_actualizacion") != null ? 
                        rs.getTimestamp("fecha_ultima_actualizacion").toLocalDateTime() : null)
                    .fechaCierre(rs.getTimestamp("fecha_cierre") != null ? 
                        rs.getTimestamp("fecha_cierre").toLocalDateTime() : null)
                    .nombreEstado(rs.getString("nombre_estado"))
                    .nombrePrioridad(rs.getString("nombre_prioridad"))
                    .nombreTipoComprobante(rs.getString("nombre_tipo_comprobante"))
                    .nombreAgente(rs.getString("nombre_agente"))
                    .build();
        }
    };
    
    public List<TicketDTO> obtenerTicketsPorUsuario(Integer usuarioId) {
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("sp_obtener_tickets_por_usuario")
                .declareParameters(new SqlParameter("id_usuario_cliente", Types.INTEGER))
                .returningResultSet("tickets", ticketRowMapper);
        
        Map<String, Object> result = jdbcCall.execute(Map.of("id_usuario_cliente", usuarioId));
        
        @SuppressWarnings("unchecked")
        List<TicketDTO> tickets = (List<TicketDTO>) result.get("tickets");
        
        return tickets != null ? tickets : List.of();
    }
    
    public Integer obtenerUsuarioIdDelToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        
        String sql = "SELECT id_usuario FROM Usuarios WHERE email = ?";
        Integer usuarioId = jdbcTemplate.queryForObject(sql, Integer.class, email);
        
        if (usuarioId == null) {
            throw new RuntimeException("Usuario no encontrado: " + email);
        }
        
        return usuarioId;
    }
}