package com.facturactiva.app.config;

import com.facturactiva.app.util.Constantes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(Constantes.FRONT_URL));
        configuration.setAllowedMethods(Arrays.asList(
            Constantes.GET, 
            Constantes.POST, 
            Constantes.PUT, 
            Constantes.DELETE, 
            "OPTIONS"
        ));
        configuration.setAllowedHeaders(List.of(Constantes.ASTERISCO));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}