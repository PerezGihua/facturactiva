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
public class TicketDTO {
    private Integer idTicket;
    private String asunto;
    private String descripcion;
    private String numeroDocumentoRechazado;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaUltimaActualizacion;
    private LocalDateTime fechaCierre;
    
    // Información de catálogos
    private String nombreEstado;
    private String nombrePrioridad;
    private String nombreTipoComprobante;
    private String nombreAgente;
}