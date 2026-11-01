/*
 * Copyright (c) 2026 Label Viewer Project.
 *
 * Versión: 1.0.0
 * Fecha: 2026-11-01
 * Auditoría: Solicitud de renderizado inmutable con patrón Builder.
 */
package com.duxman.labelviewer.model;

import java.util.Objects;

/**
 * Petición de renderizado para transformar código ZPL a un formato de salida determinado.
 *
 * @param zpl       código ZPL a renderizar.
 * @param format    formato de salida deseado (PNG o PDF).
 * @param dimension dimensiones físicas y resolución de la etiqueta.
 * @author Duxman
 * @version 1.0.0
 * @since 2026-11-01
 */
public record RenderRequest(String zpl, RenderFormat format, LabelDimension dimension) {

    /**
     * Valida la integridad de la petición.
     */
    public RenderRequest {
        Objects.requireNonNull(zpl, "El código ZPL no puede ser nulo");
        if (zpl.isBlank()) {
            throw new IllegalArgumentException("El código ZPL no puede estar vacío");
        }
        Objects.requireNonNull(format, "El formato de salida no puede ser nulo");
        Objects.requireNonNull(dimension, "Las dimensiones de la etiqueta no pueden ser nulas");
    }

    /**
     * Inicia la construcción fluida de una {@link RenderRequest}.
     *
     * @return nueva instancia de {@link Builder}.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder para construir peticiones {@link RenderRequest}.
     */
    public static final class Builder {
        private String zpl;
        private RenderFormat format = RenderFormat.PNG;
        private LabelDimension dimension = LabelDimension.standard();

        public Builder zpl(String zpl) {
            this.zpl = zpl;
            return this;
        }

        public Builder format(RenderFormat format) {
            this.format = format;
            return this;
        }

        public Builder dimension(LabelDimension dimension) {
            this.dimension = dimension;
            return this;
        }

        public Builder dimension(double widthMm, double heightMm, int dpi) {
            this.dimension = new LabelDimension(widthMm, heightMm, dpi);
            return this;
        }

        public RenderRequest build() {
            return new RenderRequest(zpl, format, dimension);
        }
    }
}
