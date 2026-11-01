/*
 * Copyright (c) 2026 Label Viewer Project.
 *
 * Versión: 1.0.0
 * Fecha: 2026-11-01
 * Auditoría: Servicio principal de orquestación de renderizado ZPL mediante inyección por constructor.
 */
package com.duxman.labelviewer.service;

import com.duxman.labelviewer.exception.LabelRenderException;
import com.duxman.labelviewer.model.LabelDimension;
import com.duxman.labelviewer.model.RenderFormat;
import com.duxman.labelviewer.model.RenderRequest;
import com.duxman.labelviewer.model.RenderResult;
import com.duxman.labelviewer.renderer.ZplRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Servicio de negocio encargado de resolver la estrategia adecuada y coordinar el renderizado ZPL.
 *
 * @author Duxman
 * @version 1.0.0
 * @since 2026-11-01
 */
@Service
public class ZplRenderService {

    private static final Logger log = LoggerFactory.getLogger(ZplRenderService.class);

    private final List<ZplRenderer> renderers;

    /**
     * Inyección por constructor de la colección de estrategias de renderizado disponibles.
     *
     * @param renderers lista de renderizadores registrados como beans.
     */
    public ZplRenderService(List<ZplRenderer> renderers) {
        this.renderers = renderers;
    }

    /**
     * Renderiza una petición {@link RenderRequest} utilizando el renderizador que soporta el formato requerido.
     *
     * @param request petición de renderizado con ZPL, formato y dimensiones.
     * @return {@link RenderResult} con el contenido resultante.
     */
    public RenderResult render(RenderRequest request) {
        log.info("Procesando solicitud de renderizado para formato {}", request.format());
        ZplRenderer renderer = renderers.stream()
                .filter(r -> r.supports(request.format()))
                .findFirst()
                .orElseThrow(() -> new LabelRenderException("No existe renderizador disponible para el formato: " + request.format()));

        return renderer.render(request);
    }

    /**
     * Renderiza el contenido ZPL recibido desde un {@link InputStream} (ej. fichero subido o archivo en disco).
     *
     * @param inputStream flujo con los bytes del archivo ZPL.
     * @param format      formato de destino.
     * @param dimension   dimensiones de la etiqueta.
     * @return resultado binario renderizado.
     */
    public RenderResult renderFromStream(InputStream inputStream, RenderFormat format, LabelDimension dimension) {
        try {
            byte[] zplBytes = inputStream.readAllBytes();
            String zplContent = new String(zplBytes, StandardCharsets.UTF_8);
            RenderRequest request = new RenderRequest(zplContent, format, dimension);
            return render(request);
        } catch (IOException e) {
            log.error("Fallo al leer el flujo de entrada ZPL: {}", e.getMessage(), e);
            throw new LabelRenderException("Error de E/S al leer la etiqueta ZPL: " + e.getMessage(), e);
        }
    }
}
