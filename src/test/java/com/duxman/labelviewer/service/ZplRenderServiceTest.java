/*
 * Copyright (c) 2026 Label Viewer Project.
 *
 * Versión: 1.0.0
 * Fecha: 2026-11-01
 * Auditoría: Pruebas unitarias para el servicio de renderizado ZPL y estrategias PNG/PDF con JUnit Jupiter y Mockito.
 */
package com.duxman.labelviewer.service;

import com.duxman.labelviewer.model.LabelDimension;
import com.duxman.labelviewer.model.RenderFormat;
import com.duxman.labelviewer.model.RenderRequest;
import com.duxman.labelviewer.model.RenderResult;
import com.duxman.labelviewer.renderer.ZplPdfRenderer;
import com.duxman.labelviewer.renderer.ZplPngRenderer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas unitarias de conversión y renderizado de etiquetas ZPL en memoria.
 *
 * @author Duxman
 * @version 1.0.0
 * @since 2026-11-01
 */
class ZplRenderServiceTest {

    private ZplRenderService renderService;

    private static final String SAMPLE_ZPL = """
            ^XA
            ^FO50,50^A0N,50,50^FDPROD-001 - Articulo de Prueba^FS
            ^FO50,120^GB300,5,5^FS
            ^FO50,150^BCN,80,Y,N,N^FD123456789012^FS
            ^FO50,260^BQN,2,5^FDQA,https://example.com/p/001^FS
            ^XZ
            """;

    @BeforeEach
    void setUp() {
        ZplPngRenderer pngRenderer = new ZplPngRenderer();
        ZplPdfRenderer pdfRenderer = new ZplPdfRenderer(pngRenderer);
        renderService = new ZplRenderService(List.of(pngRenderer, pdfRenderer));
    }

    @Test
    @DisplayName("Debe renderizar una etiqueta ZPL a formato PNG con contenido binario válido")
    void testRenderToPng() {
        RenderRequest request = RenderRequest.builder()
                .zpl(SAMPLE_ZPL)
                .format(RenderFormat.PNG)
                .dimension(LabelDimension.standard())
                .build();

        RenderResult result = renderService.render(request);

        assertNotNull(result, "El resultado de renderizado no debe ser nulo");
        assertEquals(RenderFormat.PNG, result.format());
        assertEquals("image/png", result.mimeType());
        assertTrue(result.sizeBytes() > 0, "El tamaño del PNG generado debe ser superior a cero bytes");
        // Comprobar la firma mágica de archivo PNG (89 50 4E 47 0D 0A 1A 0A)
        byte[] content = result.content();
        assertEquals((byte) 0x89, content[0]);
        assertEquals((byte) 'P', content[1]);
        assertEquals((byte) 'N', content[2]);
        assertEquals((byte) 'G', content[3]);
    }

    @Test
    @DisplayName("Debe renderizar una etiqueta ZPL a formato PDF con cabecera %PDF")
    void testRenderToPdf() {
        RenderRequest request = RenderRequest.builder()
                .zpl(SAMPLE_ZPL)
                .format(RenderFormat.PDF)
                .dimension(LabelDimension.standard())
                .build();

        RenderResult result = renderService.render(request);

        assertNotNull(result, "El resultado de renderizado no debe ser nulo");
        assertEquals(RenderFormat.PDF, result.format());
        assertEquals("application/pdf", result.mimeType());
        assertTrue(result.sizeBytes() > 0, "El tamaño del PDF generado debe ser superior a cero bytes");

        // Comprobar la firma mágica de PDF (%PDF)
        String pdfHeader = new String(result.content(), 0, 4, StandardCharsets.US_ASCII);
        assertEquals("%PDF", pdfHeader);
    }

    @Test
    @DisplayName("Debe renderizar desde un InputStream simulando lectura de fichero")
    void testRenderFromStream() {
        ByteArrayInputStream bais = new ByteArrayInputStream(SAMPLE_ZPL.getBytes(StandardCharsets.UTF_8));
        RenderResult result = renderService.renderFromStream(bais, RenderFormat.PNG, LabelDimension.standard());

        assertNotNull(result);
        assertEquals(RenderFormat.PNG, result.format());
        assertTrue(result.sizeBytes() > 0);
    }
}
