/*
 * Copyright (c) 2026 Label Viewer Project.
 *
 * Versión: 1.0.0
 * Fecha: 2026-11-01
 * Auditoría: Controlador web Spring MVC para la interfaz gráfica de usuario interactiva.
 */
package com.duxman.labelviewer.controller;

import com.duxman.labelviewer.model.LabelDimension;
import com.duxman.labelviewer.model.RenderFormat;
import com.duxman.labelviewer.model.RenderRequest;
import com.duxman.labelviewer.model.RenderResult;
import com.duxman.labelviewer.service.ZplRenderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Controlador de vistas para la interfaz gráfica web en Thymeleaf.
 *
 * @author Duxman
 * @version 1.0.0
 * @since 2026-11-01
 */
@Controller
public class ZplWebController {

    private static final Logger log = LoggerFactory.getLogger(ZplWebController.class);

    private static final String DEFAULT_ZPL_SAMPLE = """
            ^XA
            ^FO50,50^A0N,40,40^FDEnvio Express - Etiqueta demo^FS
            ^FO50,110^GB450,4,4^FS
            ^FO50,140^A0N,30,30^FDCodigo de Rastreo:^FS
            ^FO50,180^BCN,90,Y,N,N^FDTRACK-987654321^FS
            ^FO50,300^BQN,2,6^FDQA,https://envios.ejemplo.com/rastreo?id=TRACK-987654321^FS
            ^FO200,320^A0N,28,28^FDEscanee QR para estado^FS
            ^FO50,450^GB450,2,2^FS
            ^FO50,470^A0N,24,24^FDPeso: 1.25 kg | Bultos: 1/1^FS
            ^XZ
            """;

    private final ZplRenderService renderService;

    public ZplWebController(ZplRenderService renderService) {
        this.renderService = renderService;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("zpl", DEFAULT_ZPL_SAMPLE);
        model.addAttribute("format", "PNG");
        model.addAttribute("widthMm", 100.0);
        model.addAttribute("heightMm", 150.0);
        model.addAttribute("dpi", 203);
        model.addAttribute("hasResult", false);
        return "index";
    }

    @PostMapping("/web/render")
    public String renderWeb(
            @RequestParam(name = "zpl", required = false) String zplText,
            @RequestParam(name = "file", required = false) MultipartFile file,
            @RequestParam(name = "format", defaultValue = "PNG") String formatStr,
            @RequestParam(name = "widthMm", defaultValue = "100.0") double widthMm,
            @RequestParam(name = "heightMm", defaultValue = "150.0") double heightMm,
            @RequestParam(name = "dpi", defaultValue = "203") int dpi,
            Model model) {

        log.info("Petición de renderizado recibida desde interfaz web");

        String effectiveZpl = zplText;
        if (file != null && !file.isEmpty()) {
            try {
                effectiveZpl = new String(file.getBytes(), StandardCharsets.UTF_8);
                log.info("Archivo ZPL cargado desde formulario web: '{}'", file.getOriginalFilename());
            } catch (IOException e) {
                log.error("Fallo al leer archivo subido en web: {}", e.getMessage(), e);
                model.addAttribute("error", "Error al leer el archivo subido: " + e.getMessage());
                return "index";
            }
        }

        if (effectiveZpl == null || effectiveZpl.isBlank()) {
            model.addAttribute("error", "Debe proporcionar código ZPL o subir un archivo válido.");
            return "index";
        }

        RenderFormat format = RenderFormat.fromString(formatStr);
        LabelDimension dimension = new LabelDimension(widthMm, heightMm, dpi);
        RenderRequest request = new RenderRequest(effectiveZpl, format, dimension);

        try {
            RenderResult result = renderService.render(request);
            String base64Data = Base64.getEncoder().encodeToString(result.content());

            model.addAttribute("zpl", effectiveZpl);
            model.addAttribute("format", formatStr);
            model.addAttribute("widthMm", widthMm);
            model.addAttribute("heightMm", heightMm);
            model.addAttribute("dpi", dpi);
            model.addAttribute("hasResult", true);
            model.addAttribute("resultMime", result.mimeType());
            model.addAttribute("base64Data", base64Data);
            model.addAttribute("executionMs", result.executionMs());
            model.addAttribute("sizeBytes", result.sizeBytes());
            model.addAttribute("isPdf", format == RenderFormat.PDF);

        } catch (Exception e) {
            log.error("Fallo al renderizar en la vista web: {}", e.getMessage(), e);
            model.addAttribute("error", "Error al renderizar etiqueta: " + e.getMessage());
        }

        return "index";
    }
}
