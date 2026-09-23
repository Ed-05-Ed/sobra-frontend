# Contrato HTTP de inventario

La API usa JSON UTF-8 bajo `/api`. Las fechas siguen ISO `YYYY-MM-DD`, los instantes ISO-8601 en UTC y los UUID se representan como texto. Las cantidades son números decimales; el backend usa `BigDecimal` y nunca `double`.

## Catálogos y estados

### Unidades

- `G`: gramos.
- `ML`: mililitros.
- `PIECE`: piezas; solo admite cantidades matemáticamente enteras, aunque `1`, `1.0` y `1.000` son equivalentes.

### Tipo de fecha

- `EXPIRATION`
- `BEST_BEFORE`

### Estado de fecha

- `UPCOMING`: faltan más de tres días.
- `PRIORITY`: faltan entre cero y tres días, ambos inclusive.
- `DATE_PASSED`: la fecha ya pasó.

`daysUntilLabelDate` es la diferencia entre la fecha local de `SOBRA_BUSINESS_ZONE` y `labelDate`; puede ser negativa. Estos estados ayudan a ordenar el inventario y **no certifican la seguridad alimentaria**.

### Tipo de movimiento

- `CONSUMED`
- `WASTED`

## Ingredientes

### `GET /api/ingredients`

Devuelve el catálogo ordenado por nombre.

```json
[
  {
    "id": "10000000-0000-4000-8000-000000000001",
    "name": "Leche",
    "unit": "ML"
  }
]
```

## Alimentos

### `GET /api/foods`

Devuelve lotes no archivados, incluidos los agotados, ordenados por `labelDate` y después por `id`.

```json
[
  {
    "id": "7cb1fb95-0c4b-45b2-a3bd-878ef04e91f0",
    "ingredientId": "10000000-0000-4000-8000-000000000001",
    "name": "Leche abierta",
    "remainingQuantity": 500.000,
    "unit": "ML",
    "labelDate": "2026-09-25",
    "dateType": "EXPIRATION",
    "createdAt": "2026-09-22T18:00:00Z",
    "updatedAt": "2026-09-22T18:10:00Z",
    "version": 1,
    "dateStatus": "PRIORITY",
    "daysUntilLabelDate": 3
  }
]
```

### `GET /api/foods/{id}`

Devuelve el mismo formato para un lote activo. Un lote archivado responde `404 FOOD_NOT_FOUND` porque ya no es un recurso activo.

### `POST /api/foods`

```json
{
  "ingredientId": "10000000-0000-4000-8000-000000000001",
  "name": "Leche abierta",
  "quantity": 1000,
  "labelDate": "2026-09-25",
  "dateType": "EXPIRATION"
}
```

Responde `201 Created`, cabecera `Location: /api/foods/{id}` y un `FoodResponse` completo. La unidad se toma del ingrediente. El nombre se guarda sin espacios al inicio o al final.

### `PATCH /api/foods/{id}`

`version` es obligatoria. `name`, `labelDate` y `dateType` son opcionales, pero debe incluirse al menos uno.

```json
{
  "version": 0,
  "name": "Leche para café",
  "labelDate": "2026-09-26"
}
```

Los campos omitidos conservan su valor. Un valor explícitamente nulo es inválido. `ingredientId`, `unit`, `quantity` y propiedades desconocidas se rechazan; no se ignoran silenciosamente. Responde `200` con el `FoodResponse` actualizado.

### `DELETE /api/foods/{id}?version=0`

Archiva con control optimista y responde `204 No Content`. No elimina el lote ni su historial y no crea un movimiento. Un segundo archivado responde `409 FOOD_ARCHIVED`; no se trata como un borrado idempotente.

## Movimientos

### `POST /api/foods/{id}/movements`

```json
{
  "operationId": "2fb73df1-02f8-4cad-af51-d089dc4df6dc",
  "type": "CONSUMED",
  "quantity": 500,
  "expectedVersion": 0
}
```

Tanto la primera ejecución como un reintento válido responden `200 OK`:

```json
{
  "movementId": "7225776f-d49b-4146-8331-f7dbdca5ec83",
  "operationId": "2fb73df1-02f8-4cad-af51-d089dc4df6dc",
  "foodId": "7cb1fb95-0c4b-45b2-a3bd-878ef04e91f0",
  "type": "CONSUMED",
  "quantity": 500.000,
  "unit": "ML",
  "occurredAt": "2026-09-22T18:10:00Z",
  "wasPriorityAtConsumption": true,
  "remainingQuantityAfter": 500.000,
  "foodVersionAfter": 1
}
```

`wasPriorityAtConsumption` solo es `true` para `CONSUMED` si el estado era `PRIORITY` al procesar la transacción. Siempre es `false` para `WASTED`.

## Validaciones de cantidad

- Alta y movimiento deben ser mayores que cero.
- El máximo es `NUMERIC(12,3)`: hasta nueve dígitos enteros y tres decimales.
- No se redondean valores fuera de escala.
- `PIECE` exige una cantidad matemáticamente entera.
- Un movimiento no puede superar el saldo ni operar sobre un lote archivado.
- `version` y `expectedVersion` no pueden ser negativas.

## Idempotencia y concurrencia

`operationId` es único globalmente. Un reintento debe repetir exactamente `foodId`, `type`, `quantity` y `expectedVersion`; las cantidades se comparan numéricamente, por lo que `1` y `1.000` coinciden.

- Repetición válida: devuelve el `movementId`, saldo y versión originales sin descontar otra vez, incluso después de movimientos posteriores o del archivado.
- Mismo `operationId` con contenido diferente: `409 IDEMPOTENCY_CONFLICT`.
- Dos peticiones simultáneas con la misma versión y operaciones distintas: solo una actualiza; la otra recibe `409 STALE_VERSION`.
- Dos peticiones simultáneas con el mismo `operationId`: se serializan en PostgreSQL, producen un movimiento y un descuento, y ambas pueden recuperar el mismo resultado confirmado.

Los movimientos históricos que pudieran existir antes de V3 conservan nulos los datos de resultado que entonces no se registraban. Su `operationId` sigue reservado, pero un reintento devuelve `409 IDEMPOTENCY_CONFLICT` porque no es posible reconstruir fielmente la respuesta original sin inventar datos.

## Errores

Todas las respuestas de error mantienen esta forma:

```json
{
  "code": "VALIDATION_ERROR",
  "message": "La petición contiene campos inválidos.",
  "details": [
    {
      "field": "quantity",
      "message": "debe ser mayor que cero"
    }
  ]
}
```

`details` siempre es un arreglo. Cada elemento identifica `field` y una explicación pública `message`; cuando el error afecta al cuerpo completo se usa `request`.

| HTTP | Códigos |
| --- | --- |
| 400 | `VALIDATION_ERROR`, `MALFORMED_REQUEST` |
| 404 | `INGREDIENT_NOT_FOUND`, `FOOD_NOT_FOUND`, `RESOURCE_NOT_FOUND` |
| 409 | `STALE_VERSION`, `INSUFFICIENT_QUANTITY`, `FOOD_ARCHIVED`, `IDEMPOTENCY_CONFLICT` |
| 500 | `INTERNAL_ERROR` con mensaje público genérico |

Las respuestas nunca incluyen stack traces, SQL ni credenciales. El diagnóstico completo se registra únicamente en el servidor.

## Flujo completo: leche

1. Consultar `GET /api/ingredients` y usar el UUID fijo de Leche.
2. Crear un lote de `1000 ML` con `POST /api/foods`; la respuesta inicia con `version: 0`.
3. Consumir `500 ML` enviando un `operationId` nuevo y `expectedVersion: 0`.
4. Conservar el mismo `operationId` para cualquier reintento de esa acción. La respuesta original indica `remainingQuantityAfter: 500.000` y `foodVersionAfter: 1`.
