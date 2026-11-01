/*
 * Copyright (c) 2026 Label Viewer Project.
 *
 * Versión: 1.0.0
 * Fecha: 2026-11-01
 * Auditoría: Renderizador de etiquetas ZPL a imagen PNG mediante Java2D y ZXing.
 */
package com.duxman.labelviewer.renderer;

import com.duxman.labelviewer.exception.LabelRenderException;
import com.duxman.labelviewer.model.RenderFormat;
import com.duxman.labelviewer.model.RenderRequest;
import com.duxman.labelviewer.model.RenderResult;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Renderizador de etiquetas ZPL a formato de imagen PNG mediante rasterización Java2D y ZXing.
 *
 * @author Duxman
 * @version 1.0.0
 * @since 2026-11-01
 */
@Component
public class ZplPngRenderer implements ZplRenderer {

    private static final Logger log = LoggerFactory.getLogger(ZplPngRenderer.class);

    private static final Pattern ZPL_COMMAND = Pattern.compile("\\^([A-Z0-9]{2})([^\\^]*)");

    @Override
    public boolean supports(RenderFormat format) {
        return format == RenderFormat.PNG;
    }

    @Override
    public RenderResult render(RenderRequest request) {
        log.info("Iniciando renderizado ZPL a PNG. Dimensiones: {}x{} mm a {} DPI",
                request.dimension().widthMm(), request.dimension().heightMm(), request.dimension().dpi());
        long startTime = System.currentTimeMillis();

        int width = request.dimension().widthDots();
        int height = request.dimension().heightDots();

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);
        Graphics2D g2d = image.createGraphics();

        try {
            // Fondo blanco nítido
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, width, height);

            // Ajustes de renderizado
            g2d.setColor(Color.BLACK);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            renderZplCommands(g2d, request.zpl());
        } finally {
            g2d.dispose();
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "PNG", baos);
            byte[] bytes = baos.toByteArray();
            long elapsed = System.currentTimeMillis() - startTime;
            log.info("Renderizado PNG finalizado con éxito en {} ms. Tamaño: {} bytes", elapsed, bytes.length);
            return new RenderResult(bytes, RenderFormat.PNG, elapsed);
        } catch (IOException e) {
            log.error("Fallo durante la codificación de la imagen PNG: {}", e.getMessage(), e);
            throw new LabelRenderException("Error al generar el buffer PNG: " + e.getMessage(), e);
        }
    }

    private void renderZplCommands(Graphics2D g2d, String zpl) {
        int x = 0;
        int y = 0;
        int fontHeight = 28;
        int fontWidth = 28;
        int barHeight = 60;
        int barWidth = 2;
        String barType = null;
        boolean barcodePending = false;
        boolean qrPending = false;

        Matcher matcher = ZPL_COMMAND.matcher(zpl);
        while (matcher.find()) {
            String command = matcher.group(1).toUpperCase();
            String params = matcher.group(2).strip();
            String[] args = params.split(",", -1);

            if (log.isTraceEnabled()) {
                log.trace("Comando ZPL interpretado: ^{}{}", command, params);
            }

            switch (command) {
                case "FO", "FT" -> {
                    x = parseNumber(args, 0, 0);
                    y = parseNumber(args, 1, 0);
                }
                case "CF" -> {
                    fontHeight = parseNumber(args, 1, fontHeight);
                    fontWidth = parseNumber(args, 2, fontHeight);
                }
                case "BY" -> {
                    barWidth = parseNumber(args, 0, barWidth);
                    barHeight = parseNumber(args, 2, barHeight);
                }
                case "BC", "B3" -> {
                    barType = command;
                    int heightIndex = command.equals("B3") ? 2 : 1;
                    barHeight = parseNumber(args, heightIndex, barHeight);
                    barcodePending = true;
                }
                case "BQ" -> {
                    qrPending = true;
                }
                case "GB" -> {
                    int boxW = Math.max(1, parseNumber(args, 0, 1));
                    int boxH = Math.max(1, parseNumber(args, 1, 1));
                    int thickness = Math.max(1, parseNumber(args, 2, 1));
                    g2d.setStroke(new BasicStroke(thickness));
                    g2d.drawRect(x, y, boxW, boxH);
                    log.debug("Caja dibujada en ({},{}) dimensiones {}x{} grosor {}", x, y, boxW, boxH, thickness);
                }
                case "FD" -> {
                    String data = cleanFieldData(params);
                    if (qrPending) {
                        drawQrCode(g2d, x, y, data);
                        qrPending = false;
                    } else if (barcodePending) {
                        drawBarcode(g2d, x, y, data, barType, barWidth, barHeight);
                        barcodePending = false;
                    } else {
                        drawText(g2d, x, y, data, fontHeight, fontWidth);
                    }
                }
                default -> {
                    // Reconocimiento de tipografías genéricas ^A0..^AZ
                    if (command.startsWith("A")) {
                        fontHeight = parseNumber(args, 1, fontHeight);
                        fontWidth = parseNumber(args, 2, fontHeight);
                    }
                }
            }
        }
    }

    private void drawText(Graphics2D g2d, int x, int y, String text, int fontHeight, int fontWidth) {
        Font font = new Font(Font.MONOSPACED, Font.BOLD, Math.max(10, fontHeight));
        g2d.setFont(font);
        // En ZPL las coordenadas FT/FO de texto sitúan la línea base o esquina
        int baselineY = y + fontHeight;
        g2d.drawString(text, x, baselineY);
        log.debug("Texto renderizado en ({},{}): '{}'", x, baselineY, text);
    }

    private void drawBarcode(Graphics2D g2d, int x, int y, String data, String barType, int barWidth, int barHeight) {
        BarcodeFormat format = "B3".equals(barType) ? BarcodeFormat.CODE_39 : BarcodeFormat.CODE_128;
        try {
            MultiFormatWriter writer = new MultiFormatWriter();
            BitMatrix matrix = writer.encode(data, format, data.length() * 14 * barWidth, barHeight);
            for (int i = 0; i < matrix.getWidth(); i++) {
                for (int j = 0; j < matrix.getHeight(); j++) {
                    if (matrix.get(i, j)) {
                        g2d.fillRect(x + i, y + j, 1, 1);
                    }
                }
            }
            log.debug("Código de barras {} renderizado en ({},{}) con valor '{}'", format, x, y, data);
        } catch (Exception e) {
            log.warn("No se pudo generar código de barras {} para '{}': {}", format, data, e.getMessage());
            // Fallback de texto si el código contiene caracteres no soportados
            drawText(g2d, x, y, "*" + data + "*", 20, 20);
        }
    }

    private void drawQrCode(Graphics2D g2d, int x, int y, String data) {
        try {
            Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.MARGIN, 1);
            MultiFormatWriter writer = new MultiFormatWriter();
            BitMatrix matrix = writer.encode(data, BarcodeFormat.QR_CODE, 120, 120, hints);
            for (int i = 0; i < matrix.getWidth(); i++) {
                for (int j = 0; j < matrix.getHeight(); j++) {
                    if (matrix.get(i, j)) {
                        g2d.fillRect(x + i, y + j, 1, 1);
                    }
                }
            }
            log.debug("Código QR renderizado en ({},{}) con datos: '{}'", x, y, data);
        } catch (Exception e) {
            log.warn("No se pudo generar código QR para '{}': {}", data, e.getMessage());
            drawText(g2d, x, y, "[QR:" + data + "]", 16, 16);
        }
    }

    private String cleanFieldData(String raw) {
        // En ZPL códigos como QA, o datos limpios
        if (raw.startsWith("QA,") || raw.startsWith("LA,")) {
            return raw.substring(3);
        }
        return raw;
    }

    private int parseNumber(String[] args, int index, int defaultValue) {
        if (args != null && args.length > index && !args[index].isBlank()) {
            try {
                return Integer.parseInt(args[index].trim());
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }
}
