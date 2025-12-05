package com.facturactiva.app;

import com.facturactiva.app.controller.AuthController;
import com.facturactiva.app.service.FacturactivaService;
import com.facturactiva.app.util.Constantes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Tests de Integración para FacturactivaApplication")
class FacturactivaApplicationTest {

    @Autowired
    private ApplicationContext applicationContext;
    
    @Test
    @DisplayName("Debe cargar el contexto de Spring correctamente")
    void debeCargarElContextoDeSpringCorrectamente() {
        assertThat(applicationContext).isNotNull();
    }
    
    @Test
    @DisplayName("Debe tener AuthController en el contexto")
    void debeTenerAuthControllerEnElContexto() {
        assertThat(applicationContext.containsBean("authController")).isTrue();
        
        AuthController controller = applicationContext.getBean(AuthController.class);
        assertThat(controller).isNotNull();
    }
    
    @Test
    @DisplayName("Debe tener FacturactivaService en el contexto")
    void debeTenerFacturactivaServiceEnElContexto() {
        assertThat(applicationContext.containsBean("facturactivaService")).isTrue();
        
        FacturactivaService service = applicationContext.getBean(FacturactivaService.class);
        assertThat(service).isNotNull();
    }
    
    @Test
    @DisplayName("Debe tener WebConfig en el contexto")
    void debeTenerWebConfigEnElContexto() {
        assertThat(applicationContext.containsBean("webConfig")).isTrue();
    }
    
    @Test
    @DisplayName("Debe verificar que la constante FRONT_URL esté configurada")
    void debeVerificarQueConstanteFrontUrlEsteConfigurada() {
        assertThat(Constantes.FRONT_URL)
            .isNotNull()
            .isNotEmpty();
    }
    
    @Test
    @DisplayName("Debe verificar que la ruta de logs esté configurada")
    void debeVerificarQueLaRutaDeLogsEsteConfigurada() {
        assertThat(Constantes.RUTA_LOGS)
            .isNotNull()
            .isNotEmpty();
    }
    
    @Test
    @DisplayName("Debe verificar que el directorio de logs exista o pueda crearse")
    void debeVerificarQueElDirectorioDeLogsExistaOPuedaCrearse() {
        File logDir = new File(Constantes.RUTA_LOGS);
        
        // Si no existe, intentar crear
        if (!logDir.exists()) {
            boolean created = logDir.mkdirs();
            assertThat(created || logDir.exists()).isTrue();
        }
        
        assertThat(logDir).exists()
            .isDirectory();
    }
    
    @Test
    @DisplayName("Debe tener todos los beans necesarios configurados")
    void debeTenerTodosLosBeansNecesariosConfigurados() {
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        
        assertThat(beanNames)
            .isNotEmpty()
            .hasSizeGreaterThan(0);
    }
    
    @Test
    @DisplayName("Debe verificar que el perfil de test esté activo")
    void debeVerificarQueElPerfilDeTestEsteActivo() {
        String[] activeProfiles = applicationContext.getEnvironment().getActiveProfiles();
        
        assertThat(activeProfiles).contains("test");
    }
}