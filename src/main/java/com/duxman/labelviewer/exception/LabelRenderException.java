/*
 * Copyright (c) 2026 Label Viewer Project.
 *
 * Versión: 1.0.0
 * Fecha: 2026-11-01
 * Auditoría: Excepción específica para fallos durante el proceso de renderizado ZPL.
 */
package com.duxman.labelviewer.exception;

/**
 * Excepción de dominio lanzada cuando ocurre un error en el procesamiento o renderizado de etiquetas ZPL.
 *
 * @author Duxman
 * @version 1.0.0
 * @since 2026-11-01
 */
public class LabelRenderException extends RuntimeException {

    /**
     * Construye una nueva excepción con el mensaje explicativo.
     *
     * @param message mensaje descriptivo del fallo.
     */
    public LabelRenderException(String message) {
        super(message);
    }

    /**
     * Construye una nueva excepción con mensaje explicativo y causa raíz.
     *
     * @param message mensaje descriptivo del fallo.
     * @param cause   excepción original causante.
     */
    public LabelRenderException(String message, Throwable cause) {
        super(message, cause);
    }
}
