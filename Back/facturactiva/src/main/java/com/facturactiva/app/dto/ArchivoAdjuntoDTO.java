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
public class ArchivoAdjuntoDTO {
    private Integer idArchivo;
    private String nombreArchivo;
    private String rutaAlmacenamiento;
    private Boolean esCorreccion;
    private LocalDateTime fechaSubida;
    private String nombreUsuario;
}