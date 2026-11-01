/*
 * Copyright (c) 2026 Label Viewer Project.
 *
 * Versión: 1.0.0
 * Fecha: 2026-11-01
 * Auditoría: Renderizador de etiquetas ZPL a documento vectorial PDF mediante Apache PDFBox.
 */
package com.duxman.labelviewer.renderer;

import com.duxman.labelviewer.exception.LabelRenderException;
import com.duxman.labelviewer.model.RenderFormat;
import com.duxman.labelviewer.model.RenderRequest;
import com.duxman.labelviewer.model.RenderResult;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Renderizador de etiquetas ZPL a formato PDF vectorial encapsulando la salida rasterizada en alta fidelidad.
 *
 * @author Duxman
 * @version 1.0.0
 * @since 2026-11-01
 */
@Component
public class ZplPdfRenderer implements ZplRenderer {

    private static final Logger log = LoggerFactory.getLogger(ZplPdfRenderer.class);
    private static final float POINTS_PER_MM = 72f / 25.4f;

    private final ZplPngRenderer pngRenderer;

    /**
     * Inyección por constructor del renderizador de imagen subyacente.
     *
     * @param pngRenderer renderizador PNG para la composición gráfica de alta resolución.
     */
    public ZplPdfRenderer(ZplPngRenderer pngRenderer) {
        this.pngRenderer = pngRenderer;
    }

    @Override
    public boolean supports(RenderFormat format) {
        return format == RenderFormat.PDF;
    }

    @Override
    public RenderResult render(RenderRequest request) {
        log.info("Iniciando renderizado ZPL a PDF. Dimensiones: {}x{} mm",
                request.dimension().widthMm(), request.dimension().heightMm());
        long startTime = System.currentTimeMillis();

        // Generamos primero la representación rasterizada con la resolución elegida
        RenderResult pngResult = pngRenderer.render(request);

        float pageWidthPoints = (float) (request.dimension().widthMm() * POINTS_PER_MM);
        float pageHeightPoints = (float) (request.dimension().heightMm() * POINTS_PER_MM);

        try (PDDocument document = new PDDocument()) {
            PDRectangle pageSize = new PDRectangle(pageWidthPoints, pageHeightPoints);
            PDPage page = new PDPage(pageSize);
            document.addPage(page);

            BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(pngResult.content()));
            PDImageXObject pdImage = LosslessFactory.createFromImage(document, bufferedImage);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.drawImage(pdImage, 0, 0, pageWidthPoints, pageHeightPoints);
            }

            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                document.save(baos);
                byte[] pdfBytes = baos.toByteArray();
                long elapsed = System.currentTimeMillis() - startTime;
                log.info("Renderizado PDF finalizado con éxito en {} ms. Tamaño: {} bytes", elapsed, pdfBytes.length);
                return new RenderResult(pdfBytes, RenderFormat.PDF, elapsed);
            }
        } catch (IOException e) {
            log.error("Fallo al construir el documento PDF: {}", e.getMessage(), e);
            throw new LabelRenderException("Error al ensamblar el PDF: " + e.getMessage(), e);
        }
    }
}
