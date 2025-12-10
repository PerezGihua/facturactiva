package com.facturactiva.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrearTicketRequest {
    private String documento; // numero_documento_rechazado
    private String asunto;
    private Integer tipo; // id_tipo_comprobante
    private String descripcion;
}