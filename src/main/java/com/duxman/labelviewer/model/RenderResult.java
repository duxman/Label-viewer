/*
 * Copyright (c) 2026 Label Viewer Project.
 *
 * Versión: 1.0.0
 * Fecha: 2026-11-01
 * Auditoría: Resultado binario inmutable del renderizado de etiqueta.
 */
package com.duxman.labelviewer.model;

import java.util.Objects;

/**
 * Encapsula el resultado binario obtenido tras el proceso de renderizado.
 *
 * @param content     contenido en bytes del archivo resultante (PNG o PDF).
 * @param format      formato de renderizado obtenido.
 * @param executionMs tiempo transcurrido en milisegundos para completar el renderizado.
 * @author Duxman
 * @version 1.0.0
 * @since 2026-11-01
 */
public record RenderResult(byte[] content, RenderFormat format, long executionMs) {

    public RenderResult {
        Objects.requireNonNull(content, "El contenido binario no puede ser nulo");
        Objects.requireNonNull(format, "El formato de salida no puede ser nulo");
        if (executionMs < 0) {
            throw new IllegalArgumentException("El tiempo de ejecución no puede ser negativo");
        }
    }

    /**
     * Devuelve el tamaño en bytes del resultado renderizado.
     *
     * @return tamaño en bytes.
     */
    public int sizeBytes() {
        return content.length;
    }

    /**
     * Devuelve el tipo MIME asociado al resultado.
     *
     * @return tipo MIME.
     */
    public String mimeType() {
        return format.getMimeType();
    }
}
