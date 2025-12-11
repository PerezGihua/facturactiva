package com.facturactiva.app.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgregarComentarioResponse {
    private Boolean success;
    private String message;
    private Integer idComentario;
    private Integer idRespuesta;
    private String estadoTicket;
}