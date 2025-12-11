package com.facturactiva.app.service;

import org.springframework.web.multipart.MultipartFile;

import com.facturactiva.app.dto.CrearTicketRequest;
import com.facturactiva.app.dto.DeleteTicketResponse;
import com.facturactiva.app.dto.ListTicketDTO;

import java.util.List;

public interface TicketService {
    
	List<ListTicketDTO> obtenerTicketsPorUsuario();
    
    ListTicketDTO crearTicket(CrearTicketRequest request, MultipartFile archivo);
    
    DeleteTicketResponse eliminarTicket(Integer ticketId);
}