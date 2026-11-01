/*
 * Copyright (c) 2026 Label Viewer Project.
 *
 * Versión: 1.0.0
 * Fecha: 2026-11-01
 * Auditoría: Componente CommandLineRunner para ejecución batch por línea de comandos.
 */
package com.duxman.labelviewer.cli;

import com.duxman.labelviewer.model.LabelDimension;
import com.duxman.labelviewer.model.RenderFormat;
import com.duxman.labelviewer.model.RenderRequest;
import com.duxman.labelviewer.model.RenderResult;
import com.duxman.labelviewer.service.ZplRenderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Permite ejecutar Label Viewer directamente desde la línea de comandos en modo batch.
 * <p>
 * Parámetros soportados:
 * <ul>
 *   <li>{@code --input=<ruta/archivo.zpl>} o {@code --zpl="<código ZPL>"}</li>
 *   <li>{@code --output=<ruta/salida>}</li>
 *   <li>{@code --format=PNG|PDF} (por defecto inferido de la extensión de salida o PNG)</li>
 *   <li>{@code --width=100.0} (milímetros)</li>
 *   <li>{@code --height=150.0} (milímetros)</li>
 *   <li>{@code --dpi=203} (resolución)</li>
 * </ul>
 * </p>
 *
 * @author Duxman
 * @version 1.0.0
 * @since 2026-11-01
 */
@Component
public class ZplCommandLineRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(ZplCommandLineRunner.class);

    private final ZplRenderService renderService;

    public ZplCommandLineRunner(ZplRenderService renderService) {
        this.renderService = renderService;
    }

    @Override
    public void run(ApplicationArguments args) {
        boolean hasInputFile = args.containsOption("input");
        boolean hasZplString = args.containsOption("zpl");

        if (!hasInputFile && !hasZplString) {
            log.debug("No se han proporcionado argumentos CLI de renderizado (--input o --zpl). Modo servidor web activo.");
            return;
        }

        log.info("Iniciando procesamiento por línea de comandos (CLI)...");

        String zplContent;
        if (hasInputFile) {
            String inputPathStr = args.getOptionValues("input").get(0);
            Path inputPath = Paths.get(inputPathStr);
            if (!Files.exists(inputPath)) {
                log.error("El archivo de entrada especificado no existe: {}", inputPath.toAbsolutePath());
                return;
            }
            try {
                zplContent = Files.readString(inputPath);
                log.info("Archivo ZPL cargado correctamente desde: {}", inputPath.toAbsolutePath());
            } catch (IOException e) {
                log.error("Fallo al leer el archivo de entrada {}: {}", inputPath, e.getMessage(), e);
                return;
            }
        } else {
            zplContent = args.getOptionValues("zpl").get(0);
        }

        double widthMm = parseDoubleOption(args, "width", 100.0);
        double heightMm = parseDoubleOption(args, "height", 150.0);
        int dpi = parseIntOption(args, "dpi", 203);

        String outputPathStr = args.containsOption("output") ? args.getOptionValues("output").get(0) : "etiqueta_salida.png";
        RenderFormat format = resolveFormat(args, outputPathStr);

        LabelDimension dimension = new LabelDimension(widthMm, heightMm, dpi);
        RenderRequest request = new RenderRequest(zplContent, format, dimension);

        try {
            RenderResult result = renderService.render(request);
            Path outputPath = Paths.get(outputPathStr);
            if (outputPath.getParent() != null) {
                Files.createDirectories(outputPath.getParent());
            }
            Files.write(outputPath, result.content());
            log.info("Etiqueta renderizada con éxito y guardada en: {} ({} bytes, {} ms)",
                    outputPath.toAbsolutePath(), result.sizeBytes(), result.executionMs());
        } catch (Exception e) {
            log.error("Error al renderizar la etiqueta desde CLI: {}", e.getMessage(), e);
        }
    }

    private RenderFormat resolveFormat(ApplicationArguments args, String outputPath) {
        if (args.containsOption("format")) {
            return RenderFormat.fromString(args.getOptionValues("format").get(0));
        }
        if (outputPath.toLowerCase().endsWith(".pdf")) {
            return RenderFormat.PDF;
        }
        return RenderFormat.PNG;
    }

    private double parseDoubleOption(ApplicationArguments args, String name, double defaultValue) {
        if (args.containsOption(name)) {
            try {
                return Double.parseDouble(args.getOptionValues(name).get(0));
            } catch (NumberFormatException e) {
                log.warn("Valor inválido para --{}, usando valor por defecto {}", name, defaultValue);
            }
        }
        return defaultValue;
    }

    private int parseIntOption(ApplicationArguments args, String name, int defaultValue) {
        if (args.containsOption(name)) {
            try {
                return Integer.parseInt(args.getOptionValues(name).get(0));
            } catch (NumberFormatException e) {
                log.warn("Valor inválido para --{}, usando valor por defecto {}", name, defaultValue);
            }
        }
        return defaultValue;
    }
}
