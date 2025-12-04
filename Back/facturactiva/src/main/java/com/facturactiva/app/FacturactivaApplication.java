package com.facturactiva.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.facturactiva.app.util.Constantes;

import java.io.File;

@SpringBootApplication
public class FacturactivaApplication {

    public static void main(String[] args) {
        File logDir = new File(Constantes.RUTA_LOGS);
        if (!logDir.exists()) {
            boolean created = logDir.mkdirs();
            if (created) {
                System.out.println("Carpeta de logs creada: " + logDir.getAbsolutePath());
            }
        }
        
        SpringApplication.run(FacturactivaApplication.class, args);
    }
}