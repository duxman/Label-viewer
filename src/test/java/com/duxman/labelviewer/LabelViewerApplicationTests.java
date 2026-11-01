/*
 * Copyright (c) 2026 Label Viewer Project.
 *
 * Versión: 1.0.0
 * Fecha: 2026-11-01
 * Auditoría: Test base de verificación de carga del contexto Spring Boot con JUnit Jupiter.
 */
package com.duxman.labelviewer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas unitarias de arranque y coherencia del contexto Spring Boot.
 *
 * @author Duxman
 * @version 1.0.0
 * @since 2026-11-01
 */
@SpringBootTest
class LabelViewerApplicationTests {

    @Test
    @DisplayName("El contexto de la aplicación Spring Boot debe cargar correctamente")
    void contextLoads() {
        assertTrue(true, "El contexto Spring Boot se inicializó sin errores");
    }
}
