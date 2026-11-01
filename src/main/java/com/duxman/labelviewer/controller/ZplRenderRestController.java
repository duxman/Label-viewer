/*
 * Copyright (c) 2026 Label Viewer Project.
 *
 * Versión: 1.0.0
 * Fecha: 2026-11-01
 * Auditoría: Controlador REST con documentación interactiva OpenAPI v3 para renderizado PNG y PDF.
 */
package com.duxman.labelviewer.controller;

import com.duxman.labelviewer.dto.RenderTextDto;
import com.duxman.labelviewer.model.LabelDimension;
import com.duxman.labelviewer.model.RenderFormat;
import com.duxman.labelviewer.model.RenderRequest;
import com.duxman.labelviewer.model.RenderResult;
import com.duxman.labelviewer.service.ZplRenderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Controlador REST para el servicio de conversión y visualización de etiquetas ZPL.
 *
 * @author Duxman
 * @version 1.0.0
 * @since 2026-11-01
 */
@RestController
@RequestMapping("/api/v1/render")
@Tag(name = "ZPL Render API", description = "Endpoints para convertir y renderizar etiquetas ZPL a imagen PNG o documento PDF")
public class ZplRenderRestController {

    private static final Logger log = LoggerFactory.getLogger(ZplRenderRestController.class);

    private final ZplRenderService renderService;

    /**
     * Inyección por constructor del servicio de renderizado.
     *
     * @param renderService servicio de renderizado.
     */
    public ZplRenderRestController(ZplRenderService renderService) {
        this.renderService = renderService;
    }

    @PostMapping(value = "/png", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.IMAGE_PNG_VALUE)
    @Operation(summary = "Renderizar ZPL a PNG desde JSON",
            description = "Recibe el código ZPL en texto y devuelve la imagen PNG renderizada directamente.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Imagen PNG generada con éxito",
                            content = @Content(mediaType = "image/png", schema = @Schema(type = "string", format = "binary"))),
                    @ApiResponse(responseCode = "400", description = "Código ZPL inválido o parámetros incorrectos")
            })
    public ResponseEntity<byte[]> renderPngFromJson(@Valid @RequestBody RenderTextDto dto) {
        log.info("Petición REST recibida para renderizar PNG desde JSON");
        LabelDimension dimension = new LabelDimension(dto.resolvedWidth(), dto.resolvedHeight(), dto.resolvedDpi());
        RenderRequest request = new RenderRequest(dto.zpl(), RenderFormat.PNG, dimension);
        RenderResult result = renderService.render(request);

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"etiqueta.png\"")
                .body(result.content());
    }

    @PostMapping(value = "/pdf", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Renderizar ZPL a PDF desde JSON",
            description = "Recibe el código ZPL en texto y devuelve el documento PDF vectorial renderizado.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Documento PDF generado con éxito",
                            content = @Content(mediaType = "application/pdf", schema = @Schema(type = "string", format = "binary"))),
                    @ApiResponse(responseCode = "400", description = "Código ZPL inválido o parámetros incorrectos")
            })
    public ResponseEntity<byte[]> renderPdfFromJson(@Valid @RequestBody RenderTextDto dto) {
        log.info("Petición REST recibida para renderizar PDF desde JSON");
        LabelDimension dimension = new LabelDimension(dto.resolvedWidth(), dto.resolvedHeight(), dto.resolvedDpi());
        RenderRequest request = new RenderRequest(dto.zpl(), RenderFormat.PDF, dimension);
        RenderResult result = renderService.render(request);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"etiqueta.pdf\"")
                .body(result.content());
    }

    @PostMapping(value = "/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Renderizar ZPL desde archivo subido",
            description = "Permite subir un archivo .zpl o .txt y obtener la etiqueta renderizada en PNG o PDF.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Archivo renderizado con éxito",
                            content = @Content(schema = @Schema(type = "string", format = "binary"))),
                    @ApiResponse(responseCode = "400", description = "Archivo vacío o parámetros incorrectos")
            })
    public ResponseEntity<byte[]> renderFromFile(
            @Parameter(description = "Archivo ZPL a procesar", required = true)
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "Formato deseado: PNG o PDF")
            @RequestParam(name = "format", defaultValue = "PNG") String formatStr,
            @Parameter(description = "Ancho en mm")
            @RequestParam(name = "widthMm", defaultValue = "100.0") double widthMm,
            @Parameter(description = "Alto en mm")
            @RequestParam(name = "heightMm", defaultValue = "150.0") double heightMm,
            @Parameter(description = "Resolución DPI (203, 300, 600)")
            @RequestParam(name = "dpi", defaultValue = "203") int dpi) {

        if (file.isEmpty()) {
            log.warn("Se intentó renderizar un archivo multipart vacío");
            return ResponseEntity.badRequest().build();
        }

        log.info("Petición REST recibida para renderizar archivo subido: '{}' tamaño {} bytes",
                file.getOriginalFilename(), file.getSize());

        RenderFormat format = RenderFormat.fromString(formatStr);
        LabelDimension dimension = new LabelDimension(widthMm, heightMm, dpi);

        try {
            RenderResult result = renderService.renderFromStream(file.getInputStream(), format, dimension);
            String filename = "etiqueta" + format.getFileExtension();

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(format.getMimeType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .body(result.content());
        } catch (IOException e) {
            log.error("Fallo al procesar el archivo multipart: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
