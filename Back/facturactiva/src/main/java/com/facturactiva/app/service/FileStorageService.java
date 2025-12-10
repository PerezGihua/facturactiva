package com.facturactiva.app.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class FileStorageService {
    
    private static final String UPLOAD_DIR = "C:\\facturactiva\\img";
    
    public FileStorageService() {
        // Crear el directorio si no existe
        File directory = new File(UPLOAD_DIR);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }
    
    /**
     * Guardar archivo en el servidor
     * @param file Archivo a guardar
     * @param usuarioId ID del usuario (para organizar carpetas)
     * @return Ruta del archivo guardado
     */
    public String guardarArchivo(MultipartFile file, Integer usuarioId) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("El archivo está vacío");
        }
        
        // Validar tipo de archivo (opcional)
        String contentType = file.getContentType();
        if (contentType == null || 
            (!contentType.startsWith("image/") && 
             !contentType.equals("application/pdf"))) {
            throw new IllegalArgumentException("Solo se permiten imágenes y PDFs");
        }
        
        // Crear carpeta por usuario
        String userFolder = UPLOAD_DIR + File.separator + "usuario_" + usuarioId;
        File userDirectory = new File(userFolder);
        if (!userDirectory.exists()) {
            userDirectory.mkdirs();
        }
        
        // Generar nombre único para el archivo
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String extension = getFileExtension(file.getOriginalFilename());
        String fileName = "ticket_" + timestamp + "_" + System.currentTimeMillis() + extension;
        
        // Guardar el archivo
        Path targetLocation = Paths.get(userFolder + File.separator + fileName);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        
        // Retornar la ruta relativa
        return targetLocation.toString();
    }
    
    /**
     * Obtener extensión del archivo
     */
    private String getFileExtension(String filename) {
        if (filename == null) {
            return "";
        }
        int lastDot = filename.lastIndexOf('.');
        if (lastDot == -1) {
            return "";
        }
        return filename.substring(lastDot);
    }
    
    /**
     * Eliminar archivo
     */
    public void eliminarArchivo(String rutaArchivo) {
        if (rutaArchivo == null || rutaArchivo.isEmpty()) {
            return;
        }
        
        try {
            Path path = Paths.get(rutaArchivo);
            Files.deleteIfExists(path);
        } catch (IOException e) {
            // Log del error pero no detener la ejecución
            System.err.println("Error al eliminar archivo: " + e.getMessage());
        }
    }
}