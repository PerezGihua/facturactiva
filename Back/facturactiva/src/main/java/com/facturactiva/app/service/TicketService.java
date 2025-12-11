package com.facturactiva.app.service;

import org.springframework.web.multipart.MultipartFile;

import com.facturactiva.app.dto.DetalleTicketDTO;
import com.facturactiva.app.dto.ListTicketDTO;
import com.facturactiva.app.model.AgregarComentarioRequest;
import com.facturactiva.app.model.AgregarComentarioResponse;
import com.facturactiva.app.model.CrearTicketRequest;
import com.facturactiva.app.model.DeleteTicketResponse;

import java.util.List;

public interface TicketService {
    
	List<ListTicketDTO> obtenerTicketsPorUsuario();
    
    ListTicketDTO crearTicket(CrearTicketRequest request, MultipartFile archivo);
    
    DeleteTicketResponse eliminarTicket(Integer ticketId);
    
    DetalleTicketDTO obtenerDetalleTicket(Integer ticketId);

    AgregarComentarioResponse agregarComentario(AgregarComentarioRequest request);
}