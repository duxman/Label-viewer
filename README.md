<!--
  Copyright (c) 2026 Label Viewer Project.

  Versión: 1.0.0
  Fecha: 2026-11-01
  Auditoría: Documentación inicial del proyecto Label Viewer.
-->
# Label Viewer

Sistema de visualización y renderizado de etiquetas ZPL (Zebra Programming Language) desarrollado en **Spring Boot** con **Java 21/25**. Permite convertir formatos ZPL a imágenes (**PNG**) y documentos vectoriales (**PDF**).

## Modos de Operación

1. **Línea de Comandos (CLI)**: Procesamiento por lotes y conversión directa desde terminal mediante `--input`, `--output`, `--format` y `--dpi`.
2. **API REST interactiva con OpenAPI (Swagger UI)**: Endpoints REST para procesar texto o archivos ZPL en `/swagger-ui.html`.
3. **Interfaz Gráfica Web**: Panel web interactivo en `/` para cargar archivos o pegar código ZPL, ajustar parámetros y previsualizar el resultado en tiempo real.

## Pila Tecnológica

- **Java**: Java 21 (estándar mínimo) / Java 25 LTS.
- **Framework**: Spring Boot con WebMVC y Thymeleaf.
- **Herramienta de Construcción**: Gradle.
- **Documentación API**: Springdoc OpenAPI 3 / Swagger UI.
- **Generación de Documentos**: Apache PDFBox 3.
- **Códigos de barras / 2D**: ZXing.
- **Logging**: SLF4J + Logback con rotación diaria y límite por tamaño de archivo (`logs/label-viewer-%d{yyyy-MM-dd}.%i.log`).
- **Pruebas**: JUnit Jupiter (JUnit 6 compatible) y Mockito 5.

## Estructura del Proyecto

```text
├── gradlew
├── gradlew.bat
├── gradle
│   └── wrapper
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
├── build.gradle
├── settings.gradle
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com/duxman/labelviewer
│   │   │       ├── LabelViewerApplication.java
│   │   │       ├── cli
│   │   │       │   └── ZplCommandLineRunner.java
│   │   │       ├── config
│   │   │       │   └── OpenApiConfig.java
│   │   │       ├── controller
│   │   │       │   ├── ZplRenderRestController.java
│   │   │       │   └── ZplWebController.java
│   │   │       ├── converter
│   │   │       │   └── ZplGodexConverter.java
│   │   │       ├── dto
│   │   │       │   └── RenderTextDto.java
│   │   │       ├── exception
│   │   │       │   └── LabelRenderException.java
│   │   │       ├── model
│   │   │       │   ├── LabelDimension.java
│   │   │       │   ├── RenderFormat.java
│   │   │       │   ├── RenderRequest.java
│   │   │       │   └── RenderResult.java
│   │   │       ├── renderer
│   │   │       │   ├── ZplPdfRenderer.java
│   │   │       │   ├── ZplPngRenderer.java
│   │   │       │   └── ZplRenderer.java
│   │   │       └── service
│   │   │           └── ZplRenderService.java
│   │   └── resources
│   │       ├── application.yml
│   │       ├── logback-spring.xml
│   │       └── templates
│   │           └── index.html
│   └── test
│       ├── java
│       │   └── com/duxman/labelviewer
│       └── resources
│           └── jmeter
│               └── label-viewer-load-test.jmx
├── README.md
├── RELEASE_NOTES.md
└── doc
    └── decisiones-arquitectura.md
```

## Compilación y Ejecución con Gradle Wrapper

Compilar el proyecto y ejecutar los tests unitarios:
```powershell
.\gradlew.bat test
```

Ejecutar la aplicación Spring Boot:
```powershell
.\gradlew.bat bootRun
```
