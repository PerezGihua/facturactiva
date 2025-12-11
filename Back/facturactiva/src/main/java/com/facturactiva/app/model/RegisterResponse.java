package com.facturactiva.app.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterResponse {
    private Integer idUsuario;
    private String email;
    private String nombres;
    private String apellidos;
    private Integer idRol;
    private String message;
    private Boolean success;
}