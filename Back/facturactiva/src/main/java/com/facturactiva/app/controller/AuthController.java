package com.facturactiva.app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.facturactiva.app.dto.LoginRequest;
import com.facturactiva.app.dto.LoginResponse;
import com.facturactiva.app.service.FacturactivaService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private FacturactivaService authService;

    @PostMapping("/login")
    public LoginResponse validarLogin(@RequestBody LoginRequest request) {
        return authService.authenticateUser(request);
    }
}
