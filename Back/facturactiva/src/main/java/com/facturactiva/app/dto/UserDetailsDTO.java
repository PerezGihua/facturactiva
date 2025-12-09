package com.facturactiva.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDetailsDTO {
    private Integer idUsuario;
    private Integer idRol;
    private String email;
    private String nombres;
    private String apellidos;
    private String nombreCompleto;
    private Boolean activo;
}