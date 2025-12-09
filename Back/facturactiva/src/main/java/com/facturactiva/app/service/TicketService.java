package com.facturactiva.app.service;

import java.util.List;

import com.facturactiva.app.dto.TicketDTO;

public interface TicketService {
    List<TicketDTO> obtenerTicketsPorUsuario();
}