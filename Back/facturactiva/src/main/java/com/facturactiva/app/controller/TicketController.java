package com.facturactiva.app.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.facturactiva.app.dto.TicketDTO;
import com.facturactiva.app.service.TicketService;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TicketController {
    
    private final TicketService ticketService;
    
    @GetMapping("/mis-tickets")
    public ResponseEntity<List<TicketDTO>> obtenerMisTickets() {
        List<TicketDTO> tickets = ticketService.obtenerTicketsPorUsuario();
        return ResponseEntity.ok(tickets);
    }
}