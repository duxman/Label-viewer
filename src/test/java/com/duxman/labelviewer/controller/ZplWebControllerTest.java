/*
 * Copyright (c) 2026 Label Viewer Project.
 *
 * Versión: 1.0.0
 * Fecha: 2026-11-01
 * Auditoría: Pruebas para la interfaz web ZplWebController con MockMvc y Thymeleaf.
 */
package com.duxman.labelviewer.controller;

import com.duxman.labelviewer.model.RenderFormat;
import com.duxman.labelviewer.model.RenderResult;
import com.duxman.labelviewer.service.ZplRenderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * Pruebas unitarias para el controlador de vistas web.
 *
 * @author Duxman
 * @version 1.0.0
 * @since 2026-11-01
 */
@WebMvcTest(ZplWebController.class)
class ZplWebControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ZplRenderService renderService;

    @Test
    @DisplayName("GET / debe renderizar la vista 'index' con los atributos del modelo")
    void testGetIndexView() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("zpl"))
                .andExpect(model().attributeExists("format"))
                .andExpect(model().attribute("hasResult", false));
    }

    @Test
    @DisplayName("POST /web/render debe procesar el formulario y añadir base64Data al modelo")
    void testPostRenderWeb() throws Exception {
        byte[] samplePng = new byte[]{(byte) 0x89, 'P', 'N', 'G'};
        when(renderService.render(any())).thenReturn(new RenderResult(samplePng, RenderFormat.PNG, 8));

        mockMvc.perform(post("/web/render")
                        .param("zpl", "^XA^FO50,50^FDTest^FS^XZ")
                        .param("format", "PNG")
                        .param("widthMm", "100.0")
                        .param("heightMm", "150.0")
                        .param("dpi", "203"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("hasResult", true))
                .andExpect(model().attributeExists("base64Data"))
                .andExpect(model().attribute("isPdf", false));
    }
}
