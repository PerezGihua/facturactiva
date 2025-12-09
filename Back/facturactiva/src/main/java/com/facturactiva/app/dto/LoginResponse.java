package com.facturactiva.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {
    private String token;
    private String tokenType = "Bearer";
    private Integer idRol;
    private String nombreCompleto;
    private String email;
    private String message;

    public LoginResponse(String token, Integer idRol, String nombreCompleto, String email, String message) {
        this.token = token;
        this.tokenType = "Bearer";
        this.idRol = idRol;
        this.nombreCompleto = nombreCompleto;
        this.email = email;
        this.message = message;
    }
}