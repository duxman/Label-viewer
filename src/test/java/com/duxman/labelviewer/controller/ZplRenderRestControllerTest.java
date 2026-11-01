/*
 * Copyright (c) 2026 Label Viewer Project.
 *
 * Versión: 1.0.0
 * Fecha: 2026-11-01
 * Auditoría: Pruebas unitarias para el controlador REST ZplRenderRestController con MockMvc.
 */
package com.duxman.labelviewer.controller;

import com.duxman.labelviewer.model.RenderFormat;
import com.duxman.labelviewer.model.RenderResult;
import com.duxman.labelviewer.service.ZplRenderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas de integración del controlador REST de renderizado ZPL.
 *
 * @author Duxman
 * @version 1.0.0
 * @since 2026-11-01
 */
@WebMvcTest(ZplRenderRestController.class)
class ZplRenderRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ZplRenderService renderService;

    @Test
    @DisplayName("POST /api/v1/render/png debe devolver imagen PNG y código 200")
    void testRenderPngEndpoint() throws Exception {
        byte[] mockBytes = new byte[]{(byte) 0x89, 'P', 'N', 'G'};
        when(renderService.render(any())).thenReturn(new RenderResult(mockBytes, RenderFormat.PNG, 15));

        String jsonPayload = """
                {
                    "zpl": "^XA^FO50,50^A0N,50,50^FDTest^FS^XZ",
                    "widthMm": 100.0,
                    "heightMm": 150.0,
                    "dpi": 203
                }
                """;

        mockMvc.perform(post("/api/v1/render/png")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG))
                .andExpect(header().string("Content-Disposition", "inline; filename=\"etiqueta.png\""));
    }

    @Test
    @DisplayName("POST /api/v1/render/pdf debe devolver documento PDF y código 200")
    void testRenderPdfEndpoint() throws Exception {
        byte[] mockBytes = "%PDF-1.5".getBytes();
        when(renderService.render(any())).thenReturn(new RenderResult(mockBytes, RenderFormat.PDF, 25));

        String jsonPayload = """
                {
                    "zpl": "^XA^FO50,50^A0N,50,50^FDTest^FS^XZ"
                }
                """;

        mockMvc.perform(post("/api/v1/render/pdf")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string("Content-Disposition", "inline; filename=\"etiqueta.pdf\""));
    }

    @Test
    @DisplayName("POST /api/v1/render/file debe procesar multipart ZPL y devolver el archivo")
    void testRenderFileEndpoint() throws Exception {
        byte[] mockBytes = new byte[]{(byte) 0x89, 'P', 'N', 'G'};
        when(renderService.renderFromStream(any(), any(), any()))
                .thenReturn(new RenderResult(mockBytes, RenderFormat.PNG, 10));

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.zpl",
                MediaType.TEXT_PLAIN_VALUE,
                "^XA^FO20,20^FDHola^FS^XZ".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/render/file")
                        .file(file)
                        .param("format", "PNG"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG));
    }
}
