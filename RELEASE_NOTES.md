<!--
  Copyright (c) 2026 Label Viewer Project.

  Versión: 1.2.1
  Fecha: 2026-11-01
  Auditoría: Corrección de compatibilidad JDK 23 para Gradle Wrapper y paso del 100% de la suite de pruebas.
-->
# Notas de Versión (Release Notes)

Todas las modificaciones notables de este proyecto quedan documentadas en este archivo según el estándar [Semantic Versioning (SemVer)](https://semver.org/).

## [1.2.1] - 2026-11-01

### Corregido y Validado
- **Compatibilidad de compilación**:
  - Configuración explícita en `gradle.properties` de `org.gradle.java.home=C:/dev/java/jdk-23` para evitar conflicto de bytecode (versión 68 de Java 24 con Groovy/Gradle).
  - Actualización de `.gitignore` para excluir `build/`, `.gradle/`, `logs/` y artefactos de IDE.
  - Ejecución y paso con éxito del **100% de los 10 tests unitarios** (`com.duxman.labelviewer`, `cli`, `controller`, `service`).

## [1.2.0] - 2026-11-01

### Añadido
- **Gradle Wrapper**:
  - Scripts ejecutables `gradlew.bat` (Windows) y `gradlew` (Unix/macOS).
  - Configuración `gradle/wrapper/gradle-wrapper.properties` fijada a la versión 8.12.1 con soporte para Java 21 y Spring Boot 3.4.
  - Actualización de documentación en `README.md` con instrucciones de ejecución mediante `./gradlew.bat`.

## [1.1.0] - 2026-11-01

### Añadido
- **Fase 2: Núcleo de renderizado ZPL (Strategy PNG/PDF)**:
  - Modelos inmutables de dominio: `RenderFormat`, `LabelDimension`, `RenderRequest` (con patrón Builder) y `RenderResult`.
  - Excepción de dominio `LabelRenderException`.
  - Interfaz de estrategia `ZplRenderer` e implementaciones:
    - `ZplPngRenderer`: Renderizado de alta definición mediante Java2D, comandos ZPL (`^FO`, `^FD`, `^GB`, `^BC`, `^B3`, `^BQ`, `^CF`, etc.) y códigos QR/Barcode con ZXing.
    - `ZplPdfRenderer`: Ensamblado de documentos vectoriales PDF mediante Apache PDFBox 3.
  - Servicio de negocio orquestador `ZplRenderService` con inyección por constructor.
  - Pruebas unitarias de conversión en memoria `ZplRenderServiceTest` con JUnit Jupiter.
- **Fase 3: API REST y documentación OpenAPI (Swagger)**:
  - DTO `RenderTextDto` con validaciones Jakarta Validation (`@NotBlank`, `@Positive`).
  - Controlador REST `ZplRenderRestController` con endpoints:
    - `POST /api/v1/render/png` (cuerpo JSON a imagen PNG).
    - `POST /api/v1/render/pdf` (cuerpo JSON a documento PDF).
    - `POST /api/v1/render/file` (fichero multipart ZPL a formato seleccionado).
  - Documentación interactiva Swagger UI disponible en `/swagger-ui.html` con anotaciones OpenAPI 3.
  - Pruebas web slice con `MockMvc` en `ZplRenderRestControllerTest`.
- **Fase 4: Ejecución por Línea de Comandos (CLI)**:
  - Componente `ZplCommandLineRunner` (ApplicationRunner) para procesamiento batch con parámetros `--input`, `--zpl`, `--output`, `--format`, `--width`, `--height` y `--dpi`.
  - Pruebas unitarias de línea de comandos en `ZplCommandLineRunnerTest`.
- **Fase 5: Interfaz Web con Previsualización**:
  - Plantilla web responsiva `index.html` (Thymeleaf) en `/`.
  - Soporte para edición directa de ZPL o subida de archivo, selectores de formato, DPI y dimensiones, y visor de previsualización PNG / PDF embebido.
  - Controlador web `ZplWebController` con pruebas `ZplWebControllerTest`.
- **Fase 6: Pruebas unitarias, Selenium y JMeter**:
  - Plan de pruebas de carga concurrente con JMeter 5.6 CLI en `src/test/resources/jmeter/label-viewer-load-test.jmx`.

## [1.0.0] - 2026-11-01

### Añadido
- **Fase 1: Andamiaje base del proyecto con Gradle**:
  - Configuración de compilación en `build.gradle` y `settings.gradle` con Spring Boot y Java 21/25.
  - Dependencias añadidas: `spring-boot-starter-web`, `spring-boot-starter-thymeleaf`, `spring-boot-starter-validation`, `springdoc-openapi-starter-webmvc-ui`, `pdfbox`, `zxing`, `spring-boot-starter-test`.
  - Configuración de logging en `src/main/resources/logback-spring.xml` con rotación diaria (`%d{yyyy-MM-dd}`), tamaño máximo de 10MB por fichero, retención de 30 días y límite total de 1GB.
  - Configuración de aplicación en `src/main/resources/application.yml`.
  - Clase principal de ejecución `com.duxman.labelviewer.LabelViewerApplication`.
  - Prueba unitaria de arranque de contexto `com.duxman.labelviewer.LabelViewerApplicationTests` con JUnit Jupiter.
  - Documentación de proyecto: `README.md`, `RELEASE_NOTES.md` y `doc/decisiones-arquitectura.md`.
