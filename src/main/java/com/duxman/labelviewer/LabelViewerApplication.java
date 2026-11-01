/*
 * Copyright (c) 2026 Label Viewer Project.
 *
 * Versión: 1.0.0
 * Fecha: 2026-11-01
 * Auditoría: Clase principal de inicialización de la aplicación Spring Boot Label Viewer.
 */
package com.duxman.labelviewer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada principal para el servicio Label Viewer.
 * <p>
 * Proporciona capacidades de renderizado de etiquetas ZPL a formatos de imagen (PNG)
 * y documento (PDF) mediante CLI, API REST y una interfaz gráfica web interactiva.
 * </p>
 *
 * @author Duxman
 * @version 1.0.0
 * @since 2026-11-01
 */
@SpringBootApplication
public class LabelViewerApplication {

    private static final Logger log = LoggerFactory.getLogger(LabelViewerApplication.class);

    /**
     * Método principal de arranque de la aplicación Spring Boot.
     *
     * @param args argumentos de línea de comandos.
     */
    public static void main(String[] args) {
        log.info("Iniciando Label Viewer Application...");
        SpringApplication.run(LabelViewerApplication.class, args);
        log.info("Label Viewer Application iniciada correctamente.");
    }
}
