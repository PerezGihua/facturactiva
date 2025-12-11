package com.facturactiva.app.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgregarComentarioRequest {
    private Integer idTicket;
    private String contenido;
    private Integer idComentarioPadre; // null si es comentario nuevo, id si es respuesta
}