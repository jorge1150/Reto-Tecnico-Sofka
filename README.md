# Reto Técnico – Microservicios (Spring Boot, JPA, Kafka, Docker)

Solución de dos microservicios:

- **customer-service**: gestión de *Clientes/Personas* (`/clientes`).
- **account-service**: gestión de *Cuentas/Movimientos* y *Reportes* (`/cuentas`, `/movimientos`, `/reportes`).

Arquitectura limpia (capas **domain / application / api / infrastructure**), patrón **Repository** con adaptadores JPA, **excepciones elegantes** (Problem Details RFC7807), pruebas (unitaria + integración con **Testcontainers**), e **infraestructura** con Docker Compose (PostgreSQL + Kafka).

---

## Requisitos

- Docker Desktop **o** Colima
- 6–8 GB de RAM asignada a Docker (por Kafka + 2 Postgres)
- JDK 21 y Maven *solo si vas a compilar fuera de Docker*

---

## Estructura de carpetas (resumen)

```
.
├─ customer-service/
│  ├─ src/main/java/com/sofka/customer
│  │  ├─ domain/{model,repository,event}
│  │  ├─ application/service
│  │  ├─ api/{controller,dto,mapper}
│  │  └─ infrastructure/{persistence,messaging}
│  └─ Dockerfile
├─ account-service/
│  ├─ src/main/java/com/sofka/account
│  │  ├─ domain/{model,repository,event}
│  │  ├─ application/service
│  │  ├─ api/{controller,dto,mapper,error}
│  │  └─ infrastructure/{persistence,messaging}
│  └─ Dockerfile
├─ docker-compose.yml

```

---

## Levantar todo con Docker

```bash
docker compose up --build
```

Servicios:

- **customer-service** → `http://localhost:8081`
- **account-service**  → `http://localhost:8082`

Health:
```bash
curl -s http://localhost:8081/actuator/health
curl -s http://localhost:8082/actuator/health
```

> **DBs** viven en la red interna del compose (no se exponen puertos por defecto).
> Si necesitas conectarte desde el host, mapea puertos en `docker-compose.yml` (ej. `15432:5432`).

---

### Endpoints (CRUD + Reporte)
### customer-service (`/clientes`)
- **GET** `/clientes` – listar
- **GET** `/clientes/{clienteId}` – obtener por ID lógico
- **GET** `/clientes/id/{id}` – obtener por PK
- **POST** `/clientes` – crear
- **PUT** `/clientes/id/{id}` – actualizar (sin cambiar `clienteId`/`identificacion`)
- **DELETE** `/clientes/id/{id}` – eliminar

### account-service (`/cuentas`, `/movimientos`, `/reportes`)
**/cuentas**
- **POST** `/cuentas` – crear
- **GET** `/cuentas?clienteId={id}` – listar por cliente
- **GET** `/cuentas/{numero}` – obtener por PK
- **PUT** `/cuentas/{numero}` – actualizar (no cambia `numero`)
- **DELETE** `/cuentas/{numero}` – elimina **solo si no tiene movimientos** (sino **409**)

**/movimientos**
- **POST** `/movimientos?cuenta={numero}` – crear (valor `+` depósito, `-` retiro)
- **GET** `/movimientos?cuenta={numero}` – listar por cuenta
- **GET** `/movimientos/{id}` – obtener por PK
- **PUT** `/movimientos/{id}` – **solo** actualizar `fecha` y `tipo` (no `valor`)
- **DELETE** `/movimientos/{id}` – **solo último movimiento** de la cuenta (sino **409**)

**/reportes**
- **GET** `/reportes?clienteId=CLI001&desde=YYYY-MM-DD&hasta=YYYY-MM-DD`

---

### Ejemplos rápidos (curl)
```bash
# Crear cliente
curl -i -X POST http://localhost:8081/clientes -H "Content-Type: application/json" -d '{
  "clienteId":"CLI001","nombre":"Jose Lema","genero":"M","edad":32,
  "identificacion":"1717123456","direccion":"Otavalo sn y principal",
  "telefono":"098254785","password":"1234"
}'

# Crear cuenta
curl -i -X POST http://localhost:8082/cuentas -H "Content-Type: application/json" -d '{
  "numero":"ACC001","tipo":"AHORROS","saldoInicial":100.00,
  "clienteId":"CLI001","clienteNombre":"Jose Lema"
}'

# Depositar
curl -i -X POST 'http://localhost:8082/movimientos?cuenta=ACC001'  -H "Content-Type: application/json" -d '{ "fecha":"2025-08-08","tipo":"DEPOSITO","valor":50.00 }'

# Retiro que puede fallar por saldo
curl -i -X POST 'http://localhost:8082/movimientos?cuenta=ACC001'  -H "Content-Type: application/json" -d '{ "fecha":"2025-08-08","tipo":"RETIRO","valor":-1000.00 }'

# Reporte
curl -s 'http://localhost:8082/reportes?clienteId=CLI001&desde=2025-08-01&hasta=2025-08-31' | jq
```
---

## Errores elegantes (Problem Details)

Todas las respuestas de error usan `application/problem+json` con un `error` estable (`saldo_no_disponible`, `validation_failed`, etc.) y `X-Correlation-Id` para trazabilidad.

Ejemplo retiro sin saldo:
```json
{
  "type": "https://errors.demo.com/saldo_no_disponible",
  "title": "Saldo no disponible",
  "status": 400,
  "detail": "Saldo no disponible para la cuenta ACC001 al intentar aplicar -1000.00",
  "error": "saldo_no_disponible",
  "cuenta": "ACC001",
  "valor": -1000.00,
  "timestamp": "2025-08-08T22:26:45.840Z"
}
```

---

## Base de datos (SQL)

Archivo consolidado: **`BaseDatos.sql`** (incluído en esta entrega).  
- Ejecuta la sección **CUSTOMER** en `customerdb` y la **ACCOUNT** en `accountdb`.
- En desarrollo puedes usar `ddl-auto: update`. En serio, usa **Flyway**.

---

## Postman

- Colección: **`Postman_collection.json`** (incluída).  
- Variables ya preconfiguradas:
  - `customer_service = http://localhost:8081`
  - `account_service  = http://localhost:8082`

Importa la colección y ejecuta: **Crear cliente → Crear cuenta → Depósito → Reporte**.

---

## Tests

### Unitario (customer-service)
- `CustomerServiceApplicationTests` valida creación y duplicados con Mockito.

### Integración (account-service)
- `AccountServiceApplicationTests` levanta la app con **Testcontainers/PostgreSQL**, crea cuenta y movimientos vía HTTP, verifica saldo y el `ProblemDetail` en retiros sin saldo.

Ejecutar local (si tienes JDK/Maven):
```bash
cd customer-service && mvn -q test
cd ../account-service && mvn -q test
```

> En CI, los tests de integración bajan imágenes oficiales y no requieren servicios previos.

---

## Troubleshooting

- **“Ports are not available … 5432”** → ya tienes Postgres local. Quita `ports:` de las BDs o usa `15432:5432` y `15433:5432`.
- **Kafka/Zookeeper unhealthy** → elimina healthchecks estrictos o usa Kafka **KRaft** (sin ZK).
- **`Schema-validation: missing table`** → en dev usa `ddl-auto: update` o aplica **Flyway** antes de arrancar.
- **`LazyInitializationException` en `/reportes`** → se solucionó con `@EntityGraph(attributePaths="cuenta")` en el repositorio y `@Transactional(readOnly = true)` en `ReporteService`.
- **“Cannot connect to the Docker daemon”** → abre Docker Desktop o `colima start`; valida con `docker version`.
- **Recursos** → asigna 6–8 GB en Docker Desktop → Settings → Resources.

---

## Notas de diseño

- **Arquitectura limpia**: capas bien separadas; dominio sin dependencias de framework.
- **Repository + Adaptadores JPA**: permite cambiar persistencia sin impactar dominio/aplicación.
- **DTO + Mapper**: no se exponen entidades en el API.
- **Asíncrono listo**: paquetes `domain.event` e `infrastructure.messaging` para publicar/consumir (Kafka).
- **Resiliencia**: manejo de errores centralizado (ProblemDetails), `X-Correlation-Id`, endpoints Actuator.
- **Escalabilidad**: servicios stateless, listos para replicar; particionamiento Kafka cuando se integre mensajería.

---

## Apagar y limpiar

```bash
docker compose down
# borrar volúmenes (datos de las BDs) si quieres resetear
docker compose down -v
```

---

## Licencia

Uso libre para fines educativos y de evaluación técnica.
