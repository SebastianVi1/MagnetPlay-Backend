package org.sebas.magnetplay.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsFilterConfig {

    @Value("${FRONTEND_URL:${app.frontend.url:}}")
    private String frontendUrl;

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);

        if (frontendUrl != null && !frontendUrl.isBlank()) {
            // Remove :80 port and trailing slashes
            String origin = frontendUrl.trim().replaceAll("/+$", "").replaceAll(":80$", "");
            config.addAllowedOrigin(origin);
        } else {
            // Fallback for development
            config.addAllowedOrigin("http://localhost:5173");
        }

        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.addExposedHeader("Authorization");
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

}
