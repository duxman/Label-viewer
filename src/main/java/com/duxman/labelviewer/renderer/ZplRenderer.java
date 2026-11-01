/*
 * Copyright (c) 2026 Label Viewer Project.
 *
 * Versión: 1.0.0
 * Fecha: 2026-11-01
 * Auditoría: Interfaz de estrategia para motores de renderizado ZPL (Patrón Strategy).
 */
package com.duxman.labelviewer.renderer;

import com.duxman.labelviewer.model.RenderFormat;
import com.duxman.labelviewer.model.RenderRequest;
import com.duxman.labelviewer.model.RenderResult;

/**
 * Estrategia de renderizado de etiquetas ZPL para un formato de salida específico.
 *
 * @author Duxman
 * @version 1.0.0
 * @since 2026-11-01
 */
public interface ZplRenderer {

    /**
     * Indica si este renderizador soporta el formato solicitado.
     *
     * @param format formato de destino.
     * @return true si es soportado, false en caso contrario.
     */
    boolean supports(RenderFormat format);

    /**
     * Ejecuta el renderizado de la solicitud ZPL dada.
     *
     * @param request datos de la petición de renderizado.
     * @return {@link RenderResult} con el contenido binario renderizado.
     */
    RenderResult render(RenderRequest request);
}
