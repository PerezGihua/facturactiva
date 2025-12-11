package com.facturactiva.app.repository;

import lombok.RequiredArgsConstructor;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Repository;

import com.facturactiva.app.dto.ArchivoAdjuntoDTO;
import com.facturactiva.app.dto.ComentarioDTO;
import com.facturactiva.app.dto.DetalleTicketDTO;
import com.facturactiva.app.dto.ListTicketDTO;
import com.facturactiva.app.dto.RespuestaComentarioDTO;
import com.facturactiva.app.model.AgregarComentarioResponse;
import com.facturactiva.app.model.DeleteTicketResponse;
import com.facturactiva.app.util.UtilClass;

import java.awt.Window.Type;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class TicketRepository {
    
    private final JdbcTemplate jdbcTemplate;
    private final UtilClass utilClass = new UtilClass();
    
    private final RowMapper<ListTicketDTO> ticketRowMapper = (rs, rowNum) -> {
        try {
            return ListTicketDTO.builder()
                    .idTicket(rs.getInt("id_ticket"))
                    .asunto(rs.getString("asunto"))
                    .descripcion(rs.getString("descripcion"))
                    .numeroDocumentoRechazado(rs.getString("numero_documento_rechazado"))
                    .rutaArchivo(rs.getString("ruta_archivo"))
                    .nombre_archivo(rs.getString("nombre_archivo"))
                    .fechaCreacion(rs.getTimestamp("fecha_creacion") != null ? 
                        rs.getTimestamp("fecha_creacion").toLocalDateTime() : null)
                    .fechaUltimaActualizacion(rs.getTimestamp("fecha_ultima_actualizacion") != null ? 
                        rs.getTimestamp("fecha_ultima_actualizacion").toLocalDateTime() : null)
                    .fechaCierre(rs.getTimestamp("fecha_cierre") != null ? 
                        rs.getTimestamp("fecha_cierre").toLocalDateTime() : null)
                    .estado(utilClass.getColumnIfExists(rs, "nombre_estado"))
                    .prioridad(utilClass.getColumnIfExists(rs, "nombre_prioridad"))
                    .tipoComprobante(utilClass.getColumnIfExists(rs, "nombre_tipo_comprobante"))
                    .agente(utilClass.getColumnIfExists(rs, "nombre_agente"))
                    .idTipoComprobante(utilClass.getIntColumnIfExists(rs, "id_tipo_comprobante"))
                    .idPrioridad(utilClass.getIntColumnIfExists(rs, "id_prioridad"))
                    .build();
        } catch (SQLException e) {
            throw new RuntimeException("Error al mapear ticket: " + e.getMessage(), e);
        }
    };
    
    public List<ListTicketDTO> obtenerTicketsPorUsuario(Integer usuarioId) {
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("sp_obtener_tickets_por_usuario")
                .declareParameters(new SqlParameter("id_usuario_cliente", Types.INTEGER))
                .returningResultSet("tickets", ticketRowMapper);
        
        Map<String, Object> result = jdbcCall.execute(Map.of("id_usuario_cliente", usuarioId));
        
        @SuppressWarnings("unchecked")
        List<ListTicketDTO> tickets = (List<ListTicketDTO>) result.get("tickets");
        
        return tickets != null ? tickets : List.of();
    }
    
    public ListTicketDTO crearTicketConArchivo(Integer usuarioId, ListTicketDTO ticketDTO, String rutaArchivo) {
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("sp_crear_ticket_con_archivo")
                .declareParameters(
                    new SqlParameter("id_usuario_cliente", Types.INTEGER),
                    new SqlParameter("id_tipo_comprobante", Types.INTEGER),
                    new SqlParameter("asunto", Types.VARCHAR),
                    new SqlParameter("descripcion", Types.VARCHAR),
                    new SqlParameter("numero_documento_rechazado", Types.VARCHAR),
                    new SqlParameter("ruta_archivo", Types.VARCHAR),
                    new SqlParameter("nombre_archivo", Types.VARCHAR),
                    new SqlParameter("id_estado", Types.INTEGER),
                    new SqlParameter("id_prioridad", Types.INTEGER)
                )
                .returningResultSet("ticket_creado", ticketRowMapper);
        
        Map<String, Object> params = new HashMap<>();
        params.put("id_usuario_cliente", usuarioId);
        params.put("id_tipo_comprobante", ticketDTO.getIdTipoComprobante());
        params.put("asunto", ticketDTO.getAsunto());
        params.put("descripcion", ticketDTO.getDescripcion());
        params.put("numero_documento_rechazado", ticketDTO.getNumeroDocumentoRechazado());
        params.put("ruta_archivo", rutaArchivo);
        params.put("nombre_archivo", ticketDTO.getNombre_archivo());
        params.put("id_estado", 1); // ABIERTO
        params.put("id_prioridad", ticketDTO.getIdPrioridad() != null ? ticketDTO.getIdPrioridad() : 2); // MEDIA por defecto
        
        Map<String, Object> result = jdbcCall.execute(params);
        
        @SuppressWarnings("unchecked")
        List<ListTicketDTO> tickets = (List<ListTicketDTO>) result.get("ticket_creado");
        
        return tickets != null && !tickets.isEmpty() ? tickets.get(0) : null;
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
    
    public DeleteTicketResponse eliminarTicket(Integer ticketId, Integer usuarioId) {
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("sp_eliminar_ticket")
                .declareParameters(
                    new SqlParameter("id_ticket", Types.INTEGER),
                    new SqlParameter("id_usuario", Types.INTEGER)
                )
                .returningResultSet("resultado", new RowMapper<DeleteTicketResponse>() {
                    @Override
                    public DeleteTicketResponse mapRow(ResultSet rs, int rowNum) throws SQLException {
                        return DeleteTicketResponse.builder()
                                .success(rs.getInt("success"))
                                .message(rs.getString("message"))
                                .idTicket(rs.getObject("id_ticket") != null ? rs.getInt("id_ticket") : null)
                                .build();
                    }
                });
        
        Map<String, Object> params = new HashMap<>();
        params.put("id_ticket", ticketId);
        params.put("id_usuario", usuarioId);
        
        Map<String, Object> result = jdbcCall.execute(params);
        
        @SuppressWarnings("unchecked")
        List<DeleteTicketResponse> responses = (List<DeleteTicketResponse>) result.get("resultado");
        
        return responses != null && !responses.isEmpty() ? responses.get(0) : null;
    }
    
	public AgregarComentarioResponse agregarComentario(Integer ticketId, Integer usuarioId, String contenido,
			Integer idComentarioPadre) {

		SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate).withProcedureName("sp_agregar_comentario_ticket")
				.declareParameters(new SqlParameter("id_ticket", Types.INTEGER),
						new SqlParameter("id_usuario", Types.INTEGER), new SqlParameter("contenido", Types.LONGVARCHAR),
						new SqlParameter("id_comentario_padre", Types.INTEGER))
				.returningResultSet("resultado", (rs, rowNum) -> {
					try {
						return AgregarComentarioResponse.builder().success(rs.getInt("success") == 1)
								.message(rs.getString("message")).idComentario((Integer) rs.getObject("idComentario"))
								.idRespuesta((Integer) rs.getObject("idRespuesta"))
								.estadoTicket(rs.getString("estadoTicket")).build();
					} catch (SQLException e) {
						throw new RuntimeException("Error al mapear respuesta: " + e.getMessage(), e);
					}
				});

		Map<String, Object> params = new HashMap<>();
		params.put("id_ticket", ticketId);
		params.put("id_usuario", usuarioId);
		params.put("contenido", contenido);
		params.put("id_comentario_padre", idComentarioPadre);

		Map<String, Object> result = jdbcCall.execute(params);

		@SuppressWarnings("unchecked")
		List<AgregarComentarioResponse> responses = (List<AgregarComentarioResponse>) result.get("resultado");

		if (responses == null || responses.isEmpty()) {
			throw new RuntimeException("Error al agregar comentario");
		}

		return responses.get(0);
	}
    
	public DetalleTicketDTO obtenerDetalleTicket(Integer ticketId, Integer usuarioId) {
		SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate).withProcedureName("sp_obtener_detalle_ticket")
				.declareParameters(new SqlParameter("id_ticket", Types.INTEGER),
						new SqlParameter("id_usuario", Types.INTEGER));

		// ResultSet mappers
		RowMapper<DetalleTicketDTO> ticketMapper = (rs, rowNum) -> {
			try {
				return DetalleTicketDTO.builder().idTicket(rs.getInt("id_ticket")).asunto(rs.getString("asunto"))
						.numeroDocumento(rs.getString("numeroDocumento")).descripcion(rs.getString("descripcion"))
						.fechaCreacion(rs.getTimestamp("fechaCreacion") != null
								? rs.getTimestamp("fechaCreacion").toLocalDateTime()
								: null)
						.estado(rs.getString("estado")).idEstado(rs.getInt("idEstado"))
						.prioridad(rs.getString("prioridad")).idPrioridad(rs.getInt("idPrioridad"))
						.tipoComprobante(rs.getString("tipoComprobante")).nombreCliente(rs.getString("nombreCliente"))
						.nombreAgente(rs.getString("nombreAgente"))
						.idUsuarioAgente((Integer) rs.getObject("idUsuarioAgente")).build();
			} catch (SQLException e) {
				throw new RuntimeException("Error al mapear detalle del ticket: " + e.getMessage(), e);
			}
		};

		RowMapper<ArchivoAdjuntoDTO> archivoMapper = (rs, rowNum) -> {
			try {
				return ArchivoAdjuntoDTO.builder().idArchivo(rs.getInt("idArchivo"))
						.nombreArchivo(rs.getString("nombreArchivo"))
						.rutaAlmacenamiento(rs.getString("rutaAlmacenamiento"))
						.esCorreccion(rs.getBoolean("esCorreccion"))
						.fechaSubida(rs.getTimestamp("fechaSubida") != null
								? rs.getTimestamp("fechaSubida").toLocalDateTime()
								: null)
						.nombreUsuario(rs.getString("nombreUsuario")).build();
			} catch (SQLException e) {
				throw new RuntimeException("Error al mapear archivo adjunto: " + e.getMessage(), e);
			}
		};

		RowMapper<ComentarioDTO> comentarioMapper = (rs, rowNum) -> {
			try {
				return ComentarioDTO.builder().idComentario(rs.getInt("idComentario"))
						.contenido(rs.getString("contenido"))
						.fechaCreacion(rs.getTimestamp("fechaCreacion") != null
								? rs.getTimestamp("fechaCreacion").toLocalDateTime()
								: null)
						.nombreUsuario(rs.getString("nombreUsuario")).idUsuario(rs.getInt("idUsuario"))
						.tipoUsuario(rs.getString("tipoUsuario")).build();
			} catch (SQLException e) {
				throw new RuntimeException("Error al mapear comentario: " + e.getMessage(), e);
			}
		};

		RowMapper<RespuestaComentarioDTO> respuestaMapper = (rs, rowNum) -> {
			try {
				return RespuestaComentarioDTO.builder().idRespuesta(rs.getInt("idRespuesta"))
						.idComentario(rs.getInt("idComentario")).contenido(rs.getString("contenido"))
						.fechaCreacion(rs.getTimestamp("fechaCreacion") != null
								? rs.getTimestamp("fechaCreacion").toLocalDateTime()
								: null)
						.nombreUsuario(rs.getString("nombreUsuario")).idUsuario(rs.getInt("idUsuario"))
						.tipoUsuario(rs.getString("tipoUsuario")).build();
			} catch (SQLException e) {
				throw new RuntimeException("Error al mapear respuesta: " + e.getMessage(), e);
			}
		};

		jdbcCall.returningResultSet("ticket", ticketMapper).returningResultSet("archivos", archivoMapper)
				.returningResultSet("comentarios", comentarioMapper).returningResultSet("respuestas", respuestaMapper);

		Map<String, Object> params = new HashMap<>();
		params.put("id_ticket", ticketId);
		params.put("id_usuario", usuarioId);

		Map<String, Object> result = jdbcCall.execute(params);

		@SuppressWarnings("unchecked")
		List<DetalleTicketDTO> ticketList = (List<DetalleTicketDTO>) result.get("ticket");

		if (ticketList == null || ticketList.isEmpty()) {
			throw new RuntimeException("Ticket no encontrado o sin permisos");
		}

		DetalleTicketDTO detalle = ticketList.get(0);

		@SuppressWarnings("unchecked")
		List<ArchivoAdjuntoDTO> archivos = (List<ArchivoAdjuntoDTO>) result.get("archivos");
		detalle.setArchivosAdjuntos(archivos != null ? archivos : new ArrayList<>());

		@SuppressWarnings("unchecked")
		List<ComentarioDTO> comentarios = (List<ComentarioDTO>) result.get("comentarios");

		@SuppressWarnings("unchecked")
		List<RespuestaComentarioDTO> respuestas = (List<RespuestaComentarioDTO>) result.get("respuestas");

		if (comentarios != null && !comentarios.isEmpty()) {
			Map<Integer, List<RespuestaComentarioDTO>> respuestasPorComentario = new HashMap<>();

			if (respuestas != null) {
				for (RespuestaComentarioDTO respuesta : respuestas) {
					respuestasPorComentario.computeIfAbsent(respuesta.getIdComentario(), k -> new ArrayList<>())
							.add(respuesta);
				}
			}

			for (ComentarioDTO comentario : comentarios) {
				List<RespuestaComentarioDTO> respuestasComentario = respuestasPorComentario
						.get(comentario.getIdComentario());
				comentario.setRespuestas(respuestasComentario != null ? respuestasComentario : new ArrayList<>());
			}
		}

		detalle.setComentarios(comentarios != null ? comentarios : new ArrayList<>());

		return detalle;
	}
}