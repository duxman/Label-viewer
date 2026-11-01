/*
 * Copyright (c) 2026 Label Viewer Project.
 *
 * Versión: 1.0.0
 * Fecha: 2026-11-01
 * Auditoría: Configuración global de OpenAPI 3 / Swagger para metadatos del servicio.
 */
package com.duxman.labelviewer.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración del catálogo y metadatos de la API OpenAPI / Swagger.
 *
 * @author Duxman
 * @version 1.0.0
 * @since 2026-11-01
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Label Viewer API - Renderizador ZPL")
                        .version("1.0.0")
                        .description("API REST para la visualización y conversión de etiquetas ZPL a imágenes PNG y documentos PDF.")
                        .contact(new Contact().name("Duxman").url("https://github.com/duxman"))
                        .license(new License().name("Apache 2.0").url("https://www.apache.org/licenses/LICENSE-2.0.html")));
    }
}
