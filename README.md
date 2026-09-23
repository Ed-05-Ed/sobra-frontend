# SOBRA

SOBRA es un prototipo universitario para registrar alimentos, conservar su historial de consumo o desperdicio y, en bloques posteriores, recomendar formas de aprovecharlos. El backend incluye hasta el **bloque 02**: persistencia y API funcional de inventario.

> El sistema sigue siendo local. No está protegido para exposición pública y no debe publicarse mediante un túnel hasta incorporar control de acceso.

## Estructura

- `backend/`: aplicación Java/Spring Boot, API y Maven Wrapper.
- `docs/architecture.md`: arquitectura y límites de módulos.
- `docs/api-contract.md`: contrato HTTP completo.
- `docs/block-01.md` y `docs/block-02.md`: decisiones y validaciones por bloque.
- `compose.yaml`: PostgreSQL 16 para desarrollo local.
- `.env.example`: valores locales de ejemplo, nunca secretos reales.

No existe frontend todavía y no se requieren Node.js ni npm.

## Requisitos

- JDK 21 configurado en IntelliJ.
- Docker Desktop con Docker Compose.
- PowerShell en Windows.
- IntelliJ IDEA. Maven global no es necesario: Maven Wrapper descarga Maven 3.9.16.

## Iniciar PostgreSQL local

Desde la raíz `SOBRA`:

```powershell
Copy-Item .env.example .env
docker compose up -d postgres
docker compose ps
```

PostgreSQL queda en `127.0.0.1:5432` y conserva datos en el volumen nombrado de Compose. `.env` está ignorado por Git.

Si `5432` está ocupado, cambia en `.env` tanto el puerto publicado como la URL usada por Spring:

```dotenv
POSTGRES_PORT=5434
SOBRA_DB_URL=jdbc:postgresql://localhost:5434/sobra
```

## Configurar IntelliJ

1. Abre directamente `backend/pom.xml` como proyecto.
2. Selecciona JDK 21 para Project SDK y Maven Runner.
3. Crea una configuración Spring Boot con `com.sobra.SobraBackendApplication`.
4. Configura estas variables:

```text
SPRING_PROFILES_ACTIVE=local;SOBRA_DB_URL=jdbc:postgresql://localhost:5432/sobra;SOBRA_DB_USER=sobra;SOBRA_DB_PASSWORD=sobra_local_password;SOBRA_BUSINESS_ZONE=America/Mexico_City;SERVER_PORT=8080
```

Docker Compose lee el `.env` junto a `compose.yaml`; Spring Boot e IntelliJ **no lo cargan automáticamente**. Copia manualmente las variables a la configuración de ejecución. El perfil `local` contiene valores de respaldo para la base y la zona de negocio predeterminada es `America/Mexico_City`.

## Ejecutar el backend

Con PostgreSQL saludable, desde la raíz:

```powershell
Set-Location backend
$env:SPRING_PROFILES_ACTIVE = 'local'
$env:SOBRA_DB_URL = 'jdbc:postgresql://localhost:5432/sobra'
$env:SOBRA_DB_USER = 'sobra'
$env:SOBRA_DB_PASSWORD = 'sobra_local_password'
$env:SOBRA_BUSINESS_ZONE = 'America/Mexico_City'
$env:SERVER_PORT = '8080'
.\mvnw.cmd spring-boot:run
```

`SERVER_PORT` es configurable y usa `8080` por omisión. Si EcoScan u otro proceso ocupa ese puerto, no lo detengas desde este proyecto: abre otra terminal y ejecuta SOBRA en `8081`:

```powershell
Set-Location backend
$env:SPRING_PROFILES_ACTIVE = 'local'
$env:SERVER_PORT = '8081'
.\mvnw.cmd spring-boot:run
```

En ese caso comprueba salud con:

```powershell
Invoke-RestMethod http://localhost:8081/actuator/health
```

Flyway aplica V1, V2 y V3 antes de que Hibernate valide el esquema con `ddl-auto=validate`. Actuator expone únicamente `/actuator/health`, sin componentes ni detalles internos.

## Probar el flujo desde PowerShell

Con SOBRA en `8081`:

```powershell
$baseUrl = 'http://localhost:8081'

$ingredients = Invoke-RestMethod "$baseUrl/api/ingredients"
$milk = $ingredients | Where-Object name -eq 'Leche'

$food = Invoke-RestMethod -Method Post -Uri "$baseUrl/api/foods" -ContentType 'application/json' -Body (@{
  ingredientId = $milk.id
  name = 'Leche abierta'
  quantity = 1000
  labelDate = (Get-Date).Date.AddDays(3).ToString('yyyy-MM-dd')
  dateType = 'EXPIRATION'
} | ConvertTo-Json)

$movement = Invoke-RestMethod -Method Post -Uri "$baseUrl/api/foods/$($food.id)/movements" -ContentType 'application/json' -Body (@{
  operationId = [guid]::NewGuid().ToString()
  type = 'CONSUMED'
  quantity = 500
  expectedVersion = $food.version
} | ConvertTo-Json)

$movement
Invoke-RestMethod "$baseUrl/api/foods/$($food.id)"
```

El contrato, ejemplos de PATCH/DELETE y códigos de error están en `docs/api-contract.md`.

## Ejecutar integración aislada

Docker Desktop debe estar activo. Las pruebas crean un PostgreSQL 16 efímero con Testcontainers, usan un puerto aleatorio y lo eliminan al terminar; no conectan ni limpian el inventario local.

```powershell
Set-Location backend
.\mvnw.cmd clean test
```

Las pruebas cubren validaciones, fechas con `Clock` fijo, idempotencia, archivado y concurrencia HTTP mediante transacciones y conexiones separadas.

## Detener PostgreSQL sin borrar datos

Desde la raíz:

```powershell
docker compose stop postgres
```

- `docker compose stop` conserva contenedor y volumen.
- `docker compose down` elimina contenedores y red, pero conserva el volumen.
- `docker compose down -v` elimina también el volumen y borra los datos locales; no lo uses si necesitas conservarlos.

## Alcance actual

No se implementan recomendaciones, IA, impacto, frontend, autenticación completa, CORS global, túneles, OCR, mapas, donaciones ni notificaciones. El siguiente trabajo debe partir de los servicios existentes y mantener las entidades JPA fuera del contrato HTTP.
