package com.facturactiva.app.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioEntity {
    private Integer idUsuario;
    private Integer idRol;
    private String email;
    private String passwordHash;
    private String nombres;
    private String apellidos;
    private OffsetDateTime fechaRegistro;
    private Boolean activo;
}