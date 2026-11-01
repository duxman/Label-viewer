/*
 * Copyright (c) 2026 Label Viewer Project.
 *
 * Versión: 1.0.0
 * Fecha: 2026-11-01
 * Auditoría: DTO para solicitudes de renderizado de texto directo vía API REST.
 */
package com.duxman.labelviewer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

/**
 * Objeto de transferencia de datos (DTO) para la petición REST de renderizado ZPL.
 *
 * @param zpl      código ZPL completo a renderizar.
 * @param widthMm  ancho de la etiqueta en milímetros (opcional, por defecto 100).
 * @param heightMm alto de la etiqueta en milímetros (opcional, por defecto 150).
 * @param dpi      resolución en puntos por pulgada (opcional, por defecto 203).
 * @author Duxman
 * @version 1.0.0
 * @since 2026-11-01
 */
@Schema(description = "Petición con código ZPL y dimensiones para renderizado")
public record RenderTextDto(
        @NotBlank(message = "El código ZPL es obligatorio")
        @Schema(description = "Código ZPL completo con etiquetas ^XA ... ^XZ",
                example = "^XA^FO50,50^A0N,40,40^FDEtiqueta de Ejemplo^FS^XZ")
        String zpl,

        @Positive(message = "El ancho debe ser un valor positivo")
        @Schema(description = "Ancho de la etiqueta en milímetros", defaultValue = "100.0", example = "100.0")
        Double widthMm,

        @Positive(message = "El alto debe ser un valor positivo")
        @Schema(description = "Alto de la etiqueta en milímetros", defaultValue = "150.0", example = "150.0")
        Double heightMm,

        @Positive(message = "La resolución DPI debe ser un valor positivo")
        @Schema(description = "Densidad de impresión DPI (203, 300, 600)", defaultValue = "203", example = "203")
        Integer dpi
) {
    public double resolvedWidth() {
        return widthMm != null ? widthMm : 100.0;
    }

    public double resolvedHeight() {
        return heightMm != null ? heightMm : 150.0;
    }

    public int resolvedDpi() {
        return dpi != null ? dpi : 203;
    }
}
