# Bloque 02: API funcional de inventario

## Implementado

- Endpoints para catálogo, alta, consulta, edición y archivado lógico de lotes.
- Registro transaccional de consumos y desperdicios con saldo no negativo.
- DTO separados de las entidades JPA y contrato de errores mediante `@RestControllerAdvice`.
- Normalización de nombres, límites `NUMERIC(12,3)` y cantidades enteras para `PIECE`.
- Cálculo de `dateStatus`, `daysUntilLabelDate` y prioridad de consumo mediante el `Clock` inyectado.
- Control optimista con `Food.version` en edición, archivado y movimientos.
- Idempotencia global con resultado persistido, comparación numérica de cantidades y recuperación posterior a otros cambios o archivado.
- Coordinación concurrente del mismo `operationId` mediante bloqueo asesor transaccional de PostgreSQL.
- `SERVER_PORT` configurable, con `8080` como valor predeterminado.
- Suite HTTP de integración contra un PostgreSQL 16 efímero de Testcontainers, independiente de la base local.

## Migración V3

`V3__add_movement_idempotency_result.sql` agrega a `food_movements`:

- `expected_version`: versión incluida en la petición original.
- `remaining_quantity_after`: saldo confirmado por esa operación.
- `food_version_after`: versión resultante del lote.

Las tres columnas son nulas para movimientos históricos existentes y deben estar todas nulas o todas presentes. No se inventan valores ni se elimina historial. Los movimientos creados desde este bloque siempre guardan el conjunto completo.

## Decisiones

- El reintento se consulta antes de validar la versión actual del lote y se vuelve a comprobar dentro de la transacción después de adquirir el bloqueo por operación.
- El bloqueo usa `pg_advisory_xact_lock` con una clave estable derivada del UUID; se libera automáticamente al finalizar la transacción.
- La restricción única de `operation_id` permanece como defensa final de base de datos.
- La recuperación de conflictos ocurre en un servicio exterior a la transacción para no consultar desde una transacción marcada `rollback-only`.
- Los agotados permanecen visibles mientras no estén archivados.
- `DELETE` sobre un lote ya archivado devuelve `409 FOOD_ARCHIVED`; `GET` lo trata como recurso activo inexistente y devuelve `404 FOOD_NOT_FOUND`.
- Las propiedades desconocidas en JSON se rechazan para impedir que una edición parezca aceptar cambios de ingrediente, unidad o cantidad.

## Pruebas y resultados

- `cd backend; .\mvnw.cmd clean test`: `BUILD SUCCESS`, 16 pruebas, 0 fallos, 0 errores y 0 omitidas.
- La suite levantó PostgreSQL 16.15 en Testcontainers, aplicó V1, V2 y V3 desde un esquema vacío y arrancó Hibernate con `ddl-auto=validate`.
- Las pruebas HTTP cubrieron alta y consulta, reglas de cantidades, estados de fecha con `Clock` fijo, edición, archivado, movimientos, saldos, errores, idempotencia y concurrencia real con conexiones separadas.
- `cd backend; .\mvnw.cmd -DskipTests package`: `BUILD SUCCESS` y JAR ejecutable generado.
- Smoke test aislado: PostgreSQL temporal propio en `127.0.0.1:55432` y SOBRA en `8081`; `/actuator/health` devolvió `{"status":"UP"}`.
- En el smoke test se registraron `1000 ML` de leche, se consumieron `500 ML` y el mismo `operationId` se repitió: ambas respuestas conservaron el mismo `movementId`, saldo `500.000` y versión `1`.
- Un segundo arranque sobre esa misma base confirmó tres migraciones válidas, esquema en versión 3 sin migraciones pendientes y conservación del saldo.
- Los procesos y el contenedor temporales del smoke test se detuvieron; los contenedores `ecoscan-*` permanecieron intactos.

## Pendientes

- Autenticación y protección antes de cualquier exposición pública.
- Casos de uso de recomendaciones y futura interfaz `RecommendationProvider`.
- Indicadores del módulo `impact` basados en movimientos confirmados.
- Frontend y cualquier integración futura de IA, OCR, mapas, donaciones o notificaciones.

La API sigue siendo local y los estados de fecha no constituyen una certificación de seguridad alimentaria.
