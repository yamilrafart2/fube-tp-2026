# TiendaOnline — Sistema de Gestión de Pedidos, Inventario y Clientes

Monorepo con arquitectura de microservicios para el Trabajo Práctico de **Fundamentos de Desarrollo Backend**.

## Estructura del repositorio

```text
fube-tp-2026/
├── servicio-clientes/        # API de clientes
├── servicio-inventario/      # API de stock e inventario
├── servicio-pedidos/         # API de pedidos
└── observabilidad/           # Stack Docker: Grafana, Loki y Prometheus
```

El repositorio es un multi-project build de Gradle con **Spring Boot 3.3.4** y **Java 21**. Cada microservicio cuenta con su propia base de datos PostgreSQL 16+ versionada con **Flyway** (`ddl-auto: validate`), sin claves foráneas entre bases. Toda la comunicación entre servicios se realiza de forma síncrona por HTTP mediante `RestClient`.

Para abrir el proyecto en IntelliJ: **File → Open** sobre la carpeta raíz del repositorio (no sobre cada subcarpeta) para importar los tres módulos Gradle a la vez.

## Servicios, puertos y documentación Swagger

| Servicio              | Puerto | Base de datos   | Swagger UI                            |
|-----------------------|--------|-----------------|---------------------------------------|
| `servicio-clientes`   | `8081` | `clientes_db`   | http://localhost:8081/swagger-ui.html |
| `servicio-inventario` | `8082` | `inventario_db` | http://localhost:8082/swagger-ui.html |
| `servicio-pedidos`    | `8083` | `pedidos_db`    | http://localhost:8083/swagger-ui.html |

Las tres bases viven en la instancia local de PostgreSQL en el puerto `5432` con usuario/contraseña `postgres` / `postgres` (solo entorno local).

## Cómo levantar el entorno completo

### 1. Base de datos PostgreSQL 16+
Crear las tres bases vacías (`clientes_db`, `inventario_db` y `pedidos_db`, owner `postgres`) mediante pgAdmin 4 o Docker:

```bash
docker run --name postgres-tp -e POSTGRES_PASSWORD=postgres -p 5432:5432 -d postgres:16
```

Al iniciar cada servicio, **Flyway** aplica automáticamente:
- `V1__*`: Creación del esquema de tablas de dominio.
- `V2__insert_datos_iniciales.sql`: Carga de datos iniciales sincronizados para pruebas.

### 2. Stack de Observabilidad (Loki + Prometheus + Grafana)
Con **Docker Desktop** corriendo, levantar el stack desde `observabilidad/`:

```bash
cd observabilidad
docker compose up -d
```

- **Grafana:** http://localhost:3000 (usuario `admin`, contraseña `admin`).

### 3. Levantar los 3 microservicios
Configurar la variable de entorno `STATSIG_SERVER_SECRET_KEY` para `servicio-clientes` (si Statsig no responde, el flag `mask-pii` opera enmascarando los datos personales por defecto).

Para el correcto funcionamiento de Grafana Loki, activar el perfil **`loki`**:

- **Por línea de comandos** (en 3 terminales desde la raíz):
  ```bash
  STATSIG_SERVER_SECRET_KEY="<statsig_key>" ./gradlew :servicio-clientes:bootRun --args='--spring.profiles.active=loki'
  ./gradlew :servicio-inventario:bootRun --args='--spring.profiles.active=loki'
  ./gradlew :servicio-pedidos:bootRun --args='--spring.profiles.active=loki'
  ```
- **Desde IntelliJ IDEA:** En **Run → Edit Configurations...** para `ServicioClientesApplication`, `ServicioInventarioApplication` y `ServicioPedidosApplication`, configurar `Active profiles: loki` y agregar `STATSIG_SERVER_SECRET_KEY` en `ServicioClientesApplication`.

## Proceso Batch de expiración de pedidos (`servicio-pedidos`)
- **Ejecución programada (`@Scheduled`):** Configuración por defecto `0 0 3 * * *`, 03:00 AM).
- **Ejecución manual:** `POST http://localhost:8083/api/v1/jobs/expiracion-pedidos`.
- Expira pedidos en estado `CREADO` con más de 24 horas de antigüedad, libera sus reservas en `servicio-inventario` y registra el resumen de leídos, expirados y fallidos al finalizar.

## Trazabilidad en Grafana Loki
Las respuestas HTTP de los tres microservicios incluyen el encabezado **`X-Trace-Id`**. Al realizar una operación que cruza microservicios, el mismo `traceId` se propaga automáticamente a `:8081` y `:8082`. En **Grafana → Explore → Loki**, se puede reconstruir toda la traza con:

```logql
{app=~"servicio-.*"} | json | traceId="<valor_del_header_X-Trace-Id>"
```

## Tests y cobertura (JaCoCo)

Ejecutar todos los tests y generar los reportes de JaCoCo:

```bash
./gradlew test jacocoTestReport jacocoTestCoverageVerification
```

Los reportes de cobertura se generan en `<modulo>/build/reports/jacoco/test/html/index.html`.

