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
public class ComentarioDTO {
    private Integer idComentario;
    private String contenido;
    private LocalDateTime fechaCreacion;
    private String nombreUsuario;
    private Integer idUsuario;
    private String tipoUsuario; // "AGENTE" o "CLIENTE"
    
    // Respuestas a este comentario
    private List<RespuestaComentarioDTO> respuestas;
}