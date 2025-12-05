package com.facturactiva.app;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.facturactiva.app.service.FacturactivaService;
import com.facturactiva.app.util.Constantes;

import java.io.File;

@SpringBootApplication
public class FacturactivaApplication {
	
	private static final Logger logger = LogManager.getLogger(FacturactivaService.class);

    public static void main(String[] args) {
        File logDir = new File(Constantes.RUTA_LOGS);
        if (!logDir.exists()) {
            boolean created = logDir.mkdirs();
            if (created) {
            	logger.info("Carpeta de logs creada: " + logDir.getAbsolutePath());
            }
        }
        
        SpringApplication.run(FacturactivaApplication.class, args);
    }
}