/*
 * Copyright (c) 2026 Label Viewer Project.
 *
 * Versión: 1.0.0
 * Fecha: 2026-11-01
 * Auditoría: Enumeración de los formatos de salida admitidos para el renderizado.
 */
package com.duxman.labelviewer.model;

/**
 * Formatos de exportación y renderizado soportados por Label Viewer.
 *
 * @author Duxman
 * @version 1.0.0
 * @since 2026-11-01
 */
public enum RenderFormat {

    /**
     * Formato de imagen rasterizada PNG (Portable Network Graphics).
     */
    PNG("image/png", ".png"),

    /**
     * Formato de documento vectorial PDF (Portable Document Format).
     */
    PDF("application/pdf", ".pdf");

    private final String mimeType;
    private final String fileExtension;

    RenderFormat(String mimeType, String fileExtension) {
        this.mimeType = mimeType;
        this.fileExtension = fileExtension;
    }

    /**
     * Devuelve el tipo MIME asociado al formato.
     *
     * @return tipo MIME en cadena.
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * Devuelve la extensión de archivo asociada al formato.
     *
     * @return extensión que incluye el punto inicial (ej. ".png").
     */
    public String getFileExtension() {
        return fileExtension;
    }

    /**
     * Resuelve el formato a partir de una cadena textual (insensible a mayúsculas/minúsculas).
     *
     * @param value nombre del formato (ej. "png", "PDF").
     * @return {@link RenderFormat} correspondiente.
     * @throws IllegalArgumentException si el formato no es reconocido.
     */
    public static RenderFormat fromString(String value) {
        if (value == null || value.isBlank()) {
            return PNG;
        }
        return RenderFormat.valueOf(value.trim().toUpperCase());
    }
}
