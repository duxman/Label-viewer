/*
 * Copyright (c) 2026 Label Viewer Project.
 *
 * Versión: 1.0.0
 * Fecha: 2026-11-01
 * Auditoría: Pruebas unitarias para el componente ZplCommandLineRunner con simulación de argumentos CLI.
 */
package com.duxman.labelviewer.cli;

import com.duxman.labelviewer.model.RenderFormat;
import com.duxman.labelviewer.model.RenderResult;
import com.duxman.labelviewer.service.ZplRenderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.DefaultApplicationArguments;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias para la ejecución en línea de comandos.
 *
 * @author Duxman
 * @version 1.0.0
 * @since 2026-11-01
 */
class ZplCommandLineRunnerTest {

    @Test
    @DisplayName("Debe procesar argumentos CLI con --zpl y generar el archivo de salida")
    void testCliExecutionWithZplParam(@TempDir Path tempDir) throws IOException {
        ZplRenderService renderService = mock(ZplRenderService.class);
        Path outputFile = tempDir.resolve("salida.png");
        byte[] expectedContent = new byte[]{(byte) 0x89, 'P', 'N', 'G'};

        when(renderService.render(any())).thenReturn(new RenderResult(expectedContent, RenderFormat.PNG, 12));

        ZplCommandLineRunner runner = new ZplCommandLineRunner(renderService);
        String[] args = {
                "--zpl=^XA^FO50,50^FDHola^FS^XZ",
                "--output=" + outputFile.toAbsolutePath(),
                "--format=PNG",
                "--width=100.0",
                "--height=150.0",
                "--dpi=203"
        };

        runner.run(new DefaultApplicationArguments(args));

        verify(renderService).render(any());
        assertTrue(Files.exists(outputFile), "El archivo de salida debe haberse creado en disco");
    }
}
