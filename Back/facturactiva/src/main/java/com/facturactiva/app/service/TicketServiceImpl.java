package com.facturactiva.app.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.facturactiva.app.dto.TicketDTO;
import com.facturactiva.app.repository.TicketRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {
    
    private final TicketRepository ticketRepository;
    
    @Override
    public List<TicketDTO> obtenerTicketsPorUsuario() {
        Integer usuarioId = ticketRepository.obtenerUsuarioIdDelToken();
        return ticketRepository.obtenerTicketsPorUsuario(usuarioId);
    }
}