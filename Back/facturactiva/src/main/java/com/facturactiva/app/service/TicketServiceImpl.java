package com.facturactiva.app.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.facturactiva.app.dto.CrearTicketRequest;
import com.facturactiva.app.dto.TicketDTO;
import com.facturactiva.app.repository.TicketRepository;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {
    
    private final TicketRepository ticketRepository;
    private final FileStorageService fileStorageService;
    
    @Override
    public List<TicketDTO> obtenerTicketsPorUsuario() {
    	Integer usuarioId = ticketRepository.obtenerUsuarioIdDelToken();
        return ticketRepository.obtenerTicketsPorUsuario(usuarioId);
    }
    
    @Override
    public TicketDTO crearTicket(CrearTicketRequest request, MultipartFile archivo) {
        try {
            Integer usuarioId = ticketRepository.obtenerUsuarioIdDelToken();
            String rutaArchivo = null;
            if (archivo != null && !archivo.isEmpty()) {
                rutaArchivo = fileStorageService.guardarArchivo(archivo, usuarioId);
            }
            
            TicketDTO ticketDTO = TicketDTO.builder()
                    .asunto(request.getAsunto())
                    .descripcion(request.getDescripcion())
                    .numeroDocumentoRechazado(request.getDocumento())
                    .idTipoComprobante(request.getTipo())
                    .idPrioridad(2) // MEDIA por defecto
                    .build();
            
            return ticketRepository.crearTicketConArchivo(usuarioId, ticketDTO, rutaArchivo);
            
        } catch (IOException e) {
            throw new RuntimeException("Error al guardar el archivo: " + e.getMessage());
        }
    }
}