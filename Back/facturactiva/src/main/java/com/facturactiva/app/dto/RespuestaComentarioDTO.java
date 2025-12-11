package com.facturactiva.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RespuestaComentarioDTO {
    private Integer idRespuesta;
    private Integer idComentario;
    private String contenido;
    private LocalDateTime fechaCreacion;
    private String nombreUsuario;
    private Integer idUsuario;
    private String tipoUsuario; // "AGENTE" o "CLIENTE"
}