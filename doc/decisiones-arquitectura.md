<!--
  Copyright (c) 2026 Label Viewer Project.

  Versión: 1.0.0
  Fecha: 2026-11-01
  Auditoría: Registro de Decisiones de Arquitectura (ADR) del proyecto Label Viewer.
-->
# Registro de Decisiones de Arquitectura (ADR)

Este documento registra las decisiones clave de arquitectura adoptadas en el proyecto Label Viewer, su justificación y su impacto técnico.

---

## ADR-001: Elección de Gradle como herramienta de construcción

- **Fecha**: 2026-11-01
- **Estado**: Aceptado (a petición del usuario)
- **Contexto**: Se requería definir el gestor de compilación y dependencias estándar para el proyecto Spring Boot.
- **Decisión**: Se selecciona **Gradle** frente a Maven, organizando el proyecto con `build.gradle` y `settings.gradle`.
- **Consecuencias**:
  - Tiempos de compilación optimizados con caché de tareas y daemon de Gradle.
  - Gestión fluida de dependencias de Spring Boot y plugins estándar.

---

## ADR-002: Java 21 como estándar mínimo y soporte para Java 25

- **Fecha**: 2026-11-01
- **Estado**: Aceptado
- **Contexto**: Requisitos de modernidad, rendimiento y seguridad para proyectos Java.
- **Decisión**: Fijar la compilación con Java toolchain 21 como baseline garantizado, con compatibilidad y preferencia por características estables de Java 25 LTS.
- **Consecuencias**:
  - Uso de records, pattern matching para switch, bloques de texto y colecciones secuenciadas.
  - Eliminación de librerías obsoletas y métodos deprecados.

---

## ADR-003: Spring Boot con WebMVC, OpenAPI y Thymeleaf

- **Fecha**: 2026-11-01
- **Estado**: Aceptado
- **Contexto**: Se necesita soporte para API REST documentada interactivamente y una interfaz gráfica web liviana integrada.
- **Decisión**: Integrar Spring Boot con `spring-boot-starter-web`, `springdoc-openapi-starter-webmvc-ui` y `spring-boot-starter-thymeleaf`.
- **Consecuencias**:
  - Swagger UI disponible en `/swagger-ui.html` para pruebas de la API.
  - Interfaz web servida directamente desde templates sin sobrecarga de frameworks front-end externos.

---

## ADR-004: Trazabilidad y rotación diaria de logs con Logback

- **Fecha**: 2026-11-01
- **Estado**: Aceptado
- **Contexto**: Observabilidad y trazabilidad en producción requerida por las directrices de ingeniería.
- **Decisión**: Configurar SLF4J + Logback con `RollingFileAppender` y `SizeAndTimeBasedRollingPolicy`:
  - Rotación diaria (`logs/label-viewer-%d{yyyy-MM-dd}.%i.log`).
  - Límite de 10MB por fichero y límite total de 1GB con retención de 30 días.
- **Consecuencias**: Evita saturación de disco y garantiza auditoría continua de operaciones.

---

## ADR-005: Patrón Strategy para motores de renderizado ZPL

- **Fecha**: 2026-11-01
- **Estado**: Aceptado
- **Contexto**: Se necesita soporte para exportar etiquetas ZPL tanto a imagen rasterizada (PNG) como a documento vectorial (PDF) de forma extensible.
- **Decisión**: Implementar la interfaz `ZplRenderer` desacoplando `ZplPngRenderer` (Java2D + ZXing) y `ZplPdfRenderer` (Apache PDFBox 3), orquestados por `ZplRenderService`.
- **Consecuencias**: Permite añadir nuevos formatos (como SVG o emuladores remotos) sin modificar los controladores ni la lógica de negocio existente.

---

## ADR-006: Triple interfaz de usuario (CLI, REST OpenAPI y Web Thymeleaf)

- **Fecha**: 2026-11-01
- **Estado**: Aceptado
- **Contexto**: El usuario solicitó uso por línea de comandos, API REST documentada y panel visual de previsualización.
- **Decisión**: Implementar `ZplCommandLineRunner` (batch CLI), `ZplRenderRestController` (Swagger en `/swagger-ui.html`) y `ZplWebController` (Thymeleaf en `/`).
- **Consecuencias**: Máxima versatilidad de uso tanto para automatización en pipelines/scripts como para uso interactivo en navegador y servicios externos.

---

## ADR-007: Configuración de JVM para Gradle daemon y mitigación de bloqueos de archivo en Windows

- **Fecha**: 2026-11-01
- **Estado**: Aceptado
- **Contexto**: Java 24 (`major version 68`) provocaba incompatibilidad con el analizador semántico de scripts de Gradle 8.12 (`Unsupported class file major version 68`).
- **Decisión**: Configurar en `gradle.properties` el runtime de compilación con `org.gradle.java.home=C:/dev/java/jdk-23` y añadir exclusiones en `.gitignore` para carpetas `build/`, `.gradle/` y `logs/`.
- **Consecuencias**: Compilación limpia, ejecución determinista y compatibilidad completa con el JDK 23 disponible en el entorno sin alterar variables de entorno globales.


