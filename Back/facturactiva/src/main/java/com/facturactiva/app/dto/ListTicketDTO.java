package com.facturactiva.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListTicketDTO {
    private Integer idTicket;
    private String asunto;
    private String descripcion;
    private String numeroDocumentoRechazado;
    private String nombre_archivo;
    private String rutaArchivo; // NUEVO CAMPO
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaUltimaActualizacion;
    private LocalDateTime fechaCierre;
    
    // Información de catálogos
    private String estado;
    private String prioridad;
    private String tipoComprobante;
    private String agente;
    
    // Para la creación desde el frontend
    private Integer idTipoComprobante;
    private Integer idPrioridad;
}