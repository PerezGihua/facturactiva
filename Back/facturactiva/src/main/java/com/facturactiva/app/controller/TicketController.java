package com.facturactiva.app.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.facturactiva.app.dto.CrearTicketRequest;
import com.facturactiva.app.dto.DeleteTicketResponse;
import com.facturactiva.app.dto.ListTicketDTO;
import com.facturactiva.app.service.TicketService;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TicketController {
    
    private final TicketService ticketService;
    
    @GetMapping("/mis-tickets")
    public ResponseEntity<List<ListTicketDTO>> obtenerMisTickets() {
        List<ListTicketDTO> tickets = ticketService.obtenerTicketsPorUsuario();
        return ResponseEntity.ok(tickets);
    }
    
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<ListTicketDTO> crearTicket(
            @RequestParam("documento") String documento,
            @RequestParam("asunto") String asunto,
            @RequestParam("tipo") Integer tipo,
            @RequestParam("descripcion") String descripcion,
            @RequestParam(value = "archivo", required = false) MultipartFile archivo) {
        
        CrearTicketRequest request = CrearTicketRequest.builder()
                .documento(documento)
                .asunto(asunto)
                .tipo(tipo)
                .descripcion(descripcion)
                .build();
        
        ListTicketDTO nuevoTicket = ticketService.crearTicket(request, archivo);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoTicket);
    }
    
    @DeleteMapping("/{ticketId}")
    public ResponseEntity<DeleteTicketResponse> eliminarTicket(@PathVariable Integer ticketId) {
        DeleteTicketResponse response = ticketService.eliminarTicket(ticketId);
        
        if (response == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(DeleteTicketResponse.builder()
                            .success(0)
                            .message("Error al eliminar el ticket")
                            .build());
        }
        
        if (response.getSuccess() == 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        
        return ResponseEntity.ok(response);
    }
}