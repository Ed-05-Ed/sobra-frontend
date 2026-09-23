# Bloque 01: base ejecutable y persistencia

## Implementado

- Proyecto Maven Wrapper con Java 21, Spring Boot 4.1.1 y aplicación `sobra-backend` en el puerto `8080`.
- Spring Web MVC, Bean Validation, Spring Data JPA, PostgreSQL, Flyway con módulo PostgreSQL y Actuator.
- Configuración YAML base y perfil `local`, `open-in-view=false`, `ddl-auto=validate` y `Clock` inyectable con zona configurable.
- PostgreSQL 16 en Docker Compose, limitado a loopback, con healthcheck y volumen nombrado.
- Migración V1 con `ingredients`, `foods` y `food_movements`, restricciones, claves foráneas sin cascada destructiva e índices de consulta.
- Migración V2 con diez ingredientes y UUID estables; no se insertan lotes ni movimientos de demostración.
- Entidades JPA con UUID asignado al construirlas, cantidades `BigDecimal`, fechas `LocalDate`, instantes `Instant`, relaciones `LAZY` y `@Version` en `Food`.
- Repositorios mínimos para ingredientes, alimentos y movimientos.
- Actuator limitado a `/actuator/health`, sin detalles ni componentes internos.
- Prueba de integración que verifica migraciones aplicadas, los diez ingredientes y la zona del `Clock` contra PostgreSQL real.

## UUID del catálogo inicial

Estos identificadores son datos de referencia estables y no deben regenerarse entre ambientes:

| Ingrediente | Unidad | UUID |
| --- | --- | --- |
| Leche | ML | `10000000-0000-4000-8000-000000000001` |
| Avena | G | `10000000-0000-4000-8000-000000000002` |
| Plátano | PIECE | `10000000-0000-4000-8000-000000000003` |
| Huevo | PIECE | `10000000-0000-4000-8000-000000000004` |
| Pan | PIECE | `10000000-0000-4000-8000-000000000005` |
| Arroz | G | `10000000-0000-4000-8000-000000000006` |
| Tomate | PIECE | `10000000-0000-4000-8000-000000000007` |
| Cebolla | PIECE | `10000000-0000-4000-8000-000000000008` |
| Yogur | ML | `10000000-0000-4000-8000-000000000009` |
| Manzana | PIECE | `10000000-0000-4000-8000-000000000010` |

## Decisiones

- Se usa Spring Boot 4.1.1, versión estable compatible con Java 21 al crear el bloque.
- El `pom.xml` del backend es autocontenido para poder abrirlo directamente en IntelliJ.
- Flyway es el único responsable de crear o cambiar tablas; JPA valida que sus mapeos coincidan.
- La unidad de un lote vive únicamente en `ingredients`. La unidad del movimiento se conserva como dato histórico.
- La restricción de cantidades enteras para movimientos `PIECE` está en PostgreSQL. La misma regla para lotes se implementará en el servicio del siguiente bloque, tal como define el alcance.
- `operation_id` es único para soportar idempotencia en el futuro servicio de movimientos.
- No se crean paquetes con clases vacías: `recommendations`, `impact` y `shared.exception` permanecen documentados hasta tener comportamiento.
- No se agregan controladores ni se exponen entidades JPA.

## Validación ejecutada

Ejecutada el 22 de septiembre de 2026 en Windows con Temurin OpenJDK 21.0.12.1:

| Comando | Resultado real |
| --- | --- |
| `backend\\mvnw.cmd -DskipTests compile` | `BUILD SUCCESS`; 11 fuentes compiladas con `release 21`. |
| `docker compose --env-file .env.example config --quiet` | Configuración válida, sin errores. |
| `docker compose --env-file .env.example up -d postgres` | PostgreSQL `16.15` quedó `healthy` y publicado solo en `127.0.0.1:5432`. |
| `backend\\mvnw.cmd clean test` | `BUILD SUCCESS`; 1 prueba, 0 fallas, 0 errores y 0 omitidas. Flyway validó V1/V2 y Hibernate inició con `ddl-auto=validate`. |
| Consulta de esquema mediante `psql` | 10 ingredientes, 0 alimentos, 0 movimientos; restricciones e índices esperados presentes. |
| Primer arranque y `GET /actuator/health` | HTTP `200` con `{"status":"UP"}`; `/actuator` y `/actuator/info` respondieron `404`. |
| Segundo arranque sobre el mismo volumen | Arranque correcto; Flyway informó esquema en versión 2 y ninguna migración necesaria. El historial permaneció en 2 filas, versión máxima 2. |

No quedan bloqueos de validación. Docker Desktop estaba instalado pero apagado y pudo iniciarse. Un contenedor local ajeno a SOBRA ocupaba el puerto `8080`; se detuvo solo durante los smoke tests y se restauró al finalizar.

## Pendientes para el bloque 02

- Definir DTOs y casos de uso de inventario sin exponer entidades JPA.
- Implementar altas y consultas de lotes activos.
- Validar en el servicio que lotes de ingredientes `PIECE` usen cantidades enteras.
- Registrar consumo y desperdicio de forma transaccional e idempotente, respetando `@Version` y conservando historial.
- Calcular prioridad con el `Clock` y la zona de negocio.
- Incorporar manejo de errores en `shared.exception` y pruebas de casos de negocio y concurrencia.
- Definir la protección de acceso antes de cualquier publicación por túnel.

Los módulos `recommendations` e `impact` siguen fuera del alcance funcional hasta sus bloques correspondientes.
