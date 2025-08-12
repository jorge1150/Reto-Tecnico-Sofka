# Reto Técnico – Microservicios (Spring Boot, JPA, Kafka, Docker)

Solución de dos microservicios:

- **customer-service**: gestión de *Clientes/Personas* (`/clientes`).
- **account-service**: gestión de *Cuentas/Movimientos* y *Reportes* (`/cuentas`, `/movimientos`, `/reportes`).

Arquitectura limpia (capas **domain / application / api / infrastructure**), patrón **Repository** con adaptadores JPA, **excepciones elegantes** (Problem Details RFC7807), pruebas (unitaria + integración con **Testcontainers** y **Kafka embebido**), e **infraestructura** con Docker Compose (PostgreSQL + Kafka).

---

## Requisitos

- Docker Desktop **o** Colima
- 6–8 GB de RAM asignada a Docker (por Kafka + 2 Postgres)
- JDK 21 y Maven *solo si vas a compilar/correr tests fuera de Docker*

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
> Si necesitas conectarte desde el host (DBeaver), mapea puertos en `docker-compose.yml` (p.ej. `15432:5432` y `15433:5432`).

---

## Endpoints (CRUD + Reporte)

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

## Mensajería (Kafka) – **ACTIVADA**

**Propósito:** propagar cambios de cliente desde *customer-service* para actualizar el **snapshot** (`clienteNombre`, etc.) en *account-service* de forma **asíncrona**.

### Tópico
Nombre versionado (configurable): `customer.events`

En ambos `application-docker.yml`:
```yaml
app:
  topics:
    customer-events: customer.events
```

### Producer (customer-service)
- Publica `ClienteActualizadoEvent` al **crear/actualizar** un cliente.
- Config:
```yaml
spring:
  kafka:
    bootstrap-servers: kafka:9092
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
```

### Consumer (account-service)
- `@KafkaListener(topics = "${app.topics.customer-events:customer.events}", ...)`
- Actualiza el snapshot de todas las cuentas del `clienteId` recibido.
- **Deserialización robusta** (evita errores por FQN distinto entre servicios):
```yaml
spring:
  kafka:
    bootstrap-servers: kafka:9092
    consumer:
      group-id: account-service
      auto-offset-reset: earliest
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      properties:
        spring.json.trusted.packages: "*"
        spring.json.use.type.headers: false
        spring.json.value.default.type: com.sofka.account.api.event.ClienteActualizadoEvent
```

### Probar el flujo
1) Crea cliente y cuenta (ver “Ejemplos rápidos”).  
2) **Actualiza** el nombre del cliente (dispara evento):
```bash
curl -i -X PUT http://localhost:8081/clientes/id/1  -H "Content-Type: application/json" -d '{
  "clienteId":"CLI001","nombre":"Jose Lema (KAFKA)",
  "genero":"M","edad":33,"identificacion":"1717123456",
  "direccion":"Dirección X","telefono":"098254786","password":"1234"
}'
```
3) Logs de **account-service** deben mostrar recepción del evento.  
4) Verifica snapshot:
```bash
curl -s http://localhost:8082/cuentas/ACC001 | jq
# "clienteNombre": "Jose Lema (KAFKA)"
```

### Troubleshooting Kafka
- **RecordDeserializationException**: usualmente por FQN distinto. Solución arriba (`use.type.headers: false` + `value.default.type`).  
- **Offsets viejos / mensajes inválidos en dev**: reinicia stack o recrea tópico/volúmenes (solo en desarrollo).

---

## Ejemplos rápidos (curl)

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

# Depósito
curl -i -X POST 'http://localhost:8082/movimientos?cuenta=ACC001' -H "Content-Type: application/json" -d '{
  "fecha":"2025-08-08","tipo":"DEPOSITO","valor":50.00
}'

# Retiro que puede fallar por saldo (400 Problem+JSON)
curl -i -X POST 'http://localhost:8082/movimientos?cuenta=ACC001' -H "Content-Type: application/json" -d '{
  "fecha":"2025-08-08","tipo":"RETIRO","valor":-1000.00
}'

# Reporte por fechas
curl -s 'http://localhost:8082/reportes?clienteId=CLI001&desde=2025-08-01&hasta=2025-08-31' | jq
```

---

## Errores elegantes (Problem Details)

Todas las respuestas de error usan `application/problem+json` con un `error` estable (`saldo_no_disponible`, `validation_failed`, `operation_not_allowed`, etc.) y `X-Correlation-Id`.

Ejemplo (retiro sin saldo):
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

Archivo consolidado: **`BaseDatos.sql`** (incluido).  
- Ejecuta la sección **CUSTOMER** en `customerdb` y la **ACCOUNT** en `accountdb`.  
- En desarrollo puedes usar `ddl-auto: update`; en productivo, usar **Flyway**.

---

## Postman

- Colección: **`Postman_collection_full.json`** (incluida).  
- Variables:
  - `customer_service = http://localhost:8081`
  - `account_service  = http://localhost:8082`
  - `clienteId`, `clientePk`, `cuentaNumero`, `movimientoId`, `desde`, `hasta`

---

## Tests

### Unitario (customer-service)
- `ClienteServiceTest` con **Mockito**: creación OK y rechazo de identificación duplicada.

### Integración HTTP (account-service)
- `MovimientoApiIntegrationTest` con **Testcontainers/PostgreSQL**: crea cuenta, depósito OK, **400** en retiro sin saldo, y reporte OK.

### Integración **Kafka embebido** (account-service)
- `ClienteEventIntegrationTest` con **@EmbeddedKafka** + **Awaitility**: publica `ClienteActualizadoEvent` y verifica que el listener actualiza el snapshot en DB (H2 en memoria para el test).

Ejecutar local (si tienes JDK/Maven):
```bash
cd customer-service && mvn -q -Dtest=ClienteServiceTest test
cd ../account-service && mvn -q -Dtest=MovimientoApiIntegrationTest test
cd ../account-service && mvn -q -Dtest=ClienteEventIntegrationTest test
```

---

## Troubleshooting

- **“Ports are not available … 5432”** → ya tienes Postgres local. Quita `ports:` o usa `15432:5432` y `15433:5432`.
- **Kafka/Zookeeper unhealthy** → en dev, usa `condition: service_started` o KRaft; reinicia si es necesario.
- **`Schema-validation: missing table`** → en dev usa `ddl-auto: update` o aplica SQL/Flyway antes.
- **`LazyInitializationException` en `/reportes`** → solucionado con `@EntityGraph` y `@Transactional(readOnly=true)`.
- **DuplicateKeyException (YAML)** → no repitas la misma clave debajo de `properties:`; YAML no admite duplicados.
- **RecordDeserializationException (Kafka)** → usa `spring.json.use.type.headers=false` + `spring.json.value.default.type` (ver sección Kafka).

---

## Notas de diseño

- **Arquitectura limpia**: dominio independiente de framework; application encapsula reglas; controllers delgados.
- **Repository + adaptadores JPA**: puedes cambiar persistencia sin tocar dominio/application.
- **DTO + Mapper**: contrato estable hacia el exterior; entidades no expuestas.
- **Asíncrono real**: eventos de cliente → actualización de snapshots en cuentas. Listo para **Outbox/Inbox** e **idempotencia**.
- **Resiliencia**: errores RFC7807, `X-Correlation-Id`, Actuator, índices en columnas de consulta, lectura eficiente.

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
