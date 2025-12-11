package com.facturactiva.app.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.facturactiva.app.dto.CrearTicketRequest;
import com.facturactiva.app.dto.DeleteTicketResponse;
import com.facturactiva.app.dto.ListTicketDTO;
import com.facturactiva.app.repository.TicketRepository;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {
    
    private final TicketRepository ticketRepository;
    private final FileStorageService fileStorageService;
    
    @Override
    public List<ListTicketDTO> obtenerTicketsPorUsuario() {
    	Integer usuarioId = ticketRepository.obtenerUsuarioIdDelToken();
        return ticketRepository.obtenerTicketsPorUsuario(usuarioId);
    }
    
    @Override
    public ListTicketDTO crearTicket(CrearTicketRequest request, MultipartFile archivo) {
        try {
            Integer usuarioId = ticketRepository.obtenerUsuarioIdDelToken();
            String rutaArchivo = null;
            String nombreArchivo = null;
            
            if (archivo != null && !archivo.isEmpty()) {
                rutaArchivo = fileStorageService.guardarArchivo(archivo, usuarioId);
            }
            
            if (rutaArchivo != null) {
                int lastSeparator = Math.max(rutaArchivo.lastIndexOf('/'), rutaArchivo.lastIndexOf('\\'));
                if (lastSeparator != -1) {
                    nombreArchivo = rutaArchivo.substring(lastSeparator + 1);
                } else {
                    nombreArchivo = null;
                }
            }
            
            ListTicketDTO ticketDTO = ListTicketDTO.builder()
                    .asunto(request.getAsunto())
                    .descripcion(request.getDescripcion())
                    .numeroDocumentoRechazado(request.getDocumento())
                    .idTipoComprobante(request.getTipo())
                    .nombre_archivo(nombreArchivo)
                    .idPrioridad(1) // MEDIA por defecto
                    .build();
            
            return ticketRepository.crearTicketConArchivo(usuarioId, ticketDTO, rutaArchivo);
            
        } catch (IOException e) {
            throw new RuntimeException("Error al guardar el archivo: " + e.getMessage());
        }
    }
    
    @Override
    public DeleteTicketResponse eliminarTicket(Integer ticketId) {
    	Integer usuarioId = ticketRepository.obtenerUsuarioIdDelToken();
        return ticketRepository.eliminarTicket(ticketId, usuarioId);
    }
}