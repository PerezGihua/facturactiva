package com.facturactiva.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetalleTicketDTO {
    // Información del ticket
    private Integer idTicket;
    private String asunto;
    private String numeroDocumento;
    private String descripcion;
    private LocalDateTime fechaCreacion;
    private String estado;
    private Integer idEstado;
    private String prioridad;
    private Integer idPrioridad;
    private String tipoComprobante;
    private String nombreCliente;
    private String nombreAgente;
    private Integer idUsuarioAgente;
    
    // Archivos adjuntos del ticket
    private List<ArchivoAdjuntoDTO> archivosAdjuntos;
    
    // Comentarios con sus respuestas
    private List<ComentarioDTO> comentarios;
}