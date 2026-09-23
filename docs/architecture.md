# Arquitectura de SOBRA

## Monolito modular

El backend inicia como un monolito modular Spring Boot. Esta decisión mantiene despliegue, transacciones y operación simples para el prototipo, sin perder límites funcionales claros en el paquete base `com.sobra`.

Los módulos previstos son:

- `inventory`: catálogo de ingredientes, lotes disponibles y movimientos de consumo o desperdicio. Contiene modelo JPA, repositorios, DTO, controladores y servicios transaccionales.
- `recommendations`: selección de recetas que aprovechen el inventario vigente. Queda documentado, pero no se crean clases vacías ni endpoints en este bloque.
- `impact`: cálculo de indicadores a partir del historial confirmado de movimientos. También queda pendiente, sin clases de relleno.
- `shared.config`: configuración transversal; actualmente contiene el `Clock` de negocio inyectable.
- `shared.exception`: traduce validaciones, conflictos y fallos inesperados a un contrato HTTP estable, sin revelar detalles internos.

Los módulos pendientes se crearán cuando tengan comportamiento real. Las entidades JPA no se usarán como respuestas HTTP: los futuros controladores deberán trabajar con DTOs y delegar las modificaciones a servicios de aplicación.

## Persistencia

PostgreSQL es la única fuente de datos. Flyway controla el esquema y sus datos de referencia; Hibernate solo lo valida al arrancar mediante `ddl-auto=validate`. No se usa una base alternativa en pruebas.

El catálogo inicial de ingredientes tiene UUID estables. Los lotes referencian al ingrediente para obtener su unidad, evitando una segunda unidad editable que pueda contradecir el catálogo. Los movimientos sí guardan la unidad utilizada como parte del registro histórico. Las relaciones JPA hacia ingrediente y alimento son `LAZY`, y las claves foráneas restringen borrados que pudieran eliminar historial.

Cada movimiento nuevo conserva la versión esperada y el saldo y versión resultantes. Esto permite responder reintentos con el resultado original incluso si el lote cambió o fue archivado después. Los movimientos anteriores a V3 conservan esos campos nulos: no se inventan resultados históricos.

## Transacciones y concurrencia

Las ediciones, archivados y descuentos usan la versión optimista de `Food`. Un movimiento actualiza saldo, marca temporal y registro histórico en una sola transacción.

La unicidad global de `operationId` sigue protegida por la base. Además, cada operación toma un `pg_advisory_xact_lock` derivado del UUID; solicitudes simultáneas con el mismo identificador quedan serializadas dentro de PostgreSQL. La comprobación idempotente se repite después de adquirir el bloqueo y antes de consultar estado o versión del lote. La recuperación tras una excepción ocurre fuera de la transacción fallida para no consultar desde una transacción marcada para rollback.

## Tiempo de negocio

La zona horaria se obtiene de `SOBRA_BUSINESS_ZONE`, con `America/Mexico_City` como valor predeterminado. `shared.config` publica un `Clock` para que los futuros servicios calculen fechas y prioridades de forma determinista y comprobable, sin dispersar llamadas directas a `LocalDate.now()` o `Instant.now()`.

## Recomendaciones

La primera implementación de recomendaciones usará un catálogo controlado de recetas. En una evolución posterior podrá declararse una interfaz `RecommendationProvider` para cambiar el origen de sugerencias sin acoplar inventario ni controladores; la interfaz no se crea todavía porque este bloque no contiene casos de uso de recomendaciones.

Una integración futura de IA solo podrá **proponer** recetas o acciones. Ninguna propuesta modificará inventario directamente: el usuario deberá confirmarla y todo cambio pasará por los servicios transaccionales del backend, con sus validaciones e historial.

## Seguridad y exposición

El backend actual es exclusivamente local. Actuator expone solo salud sin detalles internos. Antes de usar túneles o publicar el backend se incorporará protección de acceso; no se habilitan atajos de seguridad ni CORS global en esta base.
