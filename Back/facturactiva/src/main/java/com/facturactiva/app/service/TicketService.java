package com.facturactiva.app.service;

import org.springframework.web.multipart.MultipartFile;

import com.facturactiva.app.dto.CrearTicketRequest;
import com.facturactiva.app.dto.TicketDTO;

import java.util.List;

public interface TicketService {
    
	List<TicketDTO> obtenerTicketsPorUsuario();
    
    TicketDTO crearTicket(CrearTicketRequest request, MultipartFile archivo);
}