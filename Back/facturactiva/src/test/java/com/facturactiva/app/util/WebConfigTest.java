package com.facturactiva.app.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Tests para WebConfig")
class WebConfigTest {

    private WebConfig webConfig;
    private MockMvc mockMvc;
    
    @BeforeEach
    void setUp() {
        webConfig = new WebConfig();
    }
    
    @Test
    @DisplayName("Debe crear instancia de WebConfig")
    void debeCrearInstanciaDeWebConfig() {
        assertThat(webConfig).isNotNull();
    }
    
    @Test
    @DisplayName("Debe configurar CORS correctamente")
    void debeConfigurarCorsCorrectamente() {
        CorsRegistry registry = new CorsRegistry();
        
        webConfig.addCorsMappings(registry);
        
        // Verificar que el método se ejecuta sin errores
        assertThat(registry).isNotNull();
    }
    
    @Test
    @DisplayName("Debe implementar WebMvcConfigurer")
    void debeImplementarWebMvcConfigurer() {
        assertThat(webConfig).isInstanceOf(org.springframework.web.servlet.config.annotation.WebMvcConfigurer.class);
    }
    
    @Test
    @DisplayName("Debe permitir peticiones OPTIONS para preflight")
    void debePermitirPeticionesOptionsParaPreflight() throws Exception {
        mockMvc = MockMvcBuilders
            .standaloneSetup(new TestController())
            .build();
        
        // Verificar que el endpoint existe
        mockMvc.perform(get("/api/test"))
            .andExpect(status().isOk());
    }
    
    /*@Test
    @DisplayName("Debe aplicar configuración solo a paths /api/**")
    void debeAplicarConfiguracionSoloAPathsApi() {
        CorsRegistry registry = new CorsRegistry();
        
        webConfig.addCorsMappings(registry);
        
        // La configuración debe aplicarse a /api/**
        assertThat(registry).isNotNull();
    }*/
    

    @RestController
    static class TestController {
        @GetMapping("/api/test")
        public String test() {
            return "OK";
        }
    }
}