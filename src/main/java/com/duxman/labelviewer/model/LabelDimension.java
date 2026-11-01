/*
 * Copyright (c) 2026 Label Viewer Project.
 *
 * Versión: 1.0.0
 * Fecha: 2026-11-01
 * Auditoría: Modelo inmutable para representar dimensiones físicas de etiquetas.
 */
package com.duxman.labelviewer.model;

/**
 * Representa las dimensiones de una etiqueta tanto en milímetros como en puntos de impresión (dots).
 *
 * @param widthMm  ancho en milímetros.
 * @param heightMm alto en milímetros.
 * @param dpi      resolución en puntos por pulgada (DPI).
 * @author Duxman
 * @version 1.0.0
 * @since 2026-11-01
 */
public record LabelDimension(double widthMm, double heightMm, int dpi) {

    private static final double MM_PER_INCH = 25.4;

    /**
     * Constructor con validaciones de rangos físicos válidos.
     */
    public LabelDimension {
        if (widthMm <= 0) {
            throw new IllegalArgumentException("El ancho en milímetros debe ser mayor que 0");
        }
        if (heightMm <= 0) {
            throw new IllegalArgumentException("El alto en milímetros debe ser mayor que 0");
        }
        if (dpi <= 0) {
            throw new IllegalArgumentException("La resolución DPI debe ser mayor que 0");
        }
    }

    /**
     * Dimensiones por defecto para etiquetas estándar (100x150 mm a 203 DPI).
     *
     * @return instancia estándar de {@link LabelDimension}.
     */
    public static LabelDimension standard() {
        return new LabelDimension(100.0, 150.0, 203);
    }

    /**
     * Calcula el ancho total en puntos de impresión (dots/píxeles).
     *
     * @return ancho en dots.
     */
    public int widthDots() {
        return (int) Math.round((widthMm / MM_PER_INCH) * dpi);
    }

    /**
     * Calcula el alto total en puntos de impresión (dots/píxeles).
     *
     * @return alto en dots.
     */
    public int heightDots() {
        return (int) Math.round((heightMm / MM_PER_INCH) * dpi);
    }
}
