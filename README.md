# API Entidad Financiera — Prueba Técnica (Spring Boot 4 + Arquitectura Hexagonal)

Backend de una entidad financiera desarrollado con **Java 21**, **Spring Boot 4.1.2**, **PostgreSQL 16** y **Arquitectura Hexagonal (Ports & Adapters)**, diseñado para gestionar **Clientes**, **Cuentas (Ahorros y Corriente)** y **Transacciones Financieras (Consignación, Retiro y Transferencia)**.

---

## 🏛️ Arquitectura y Decisiones de Diseño

El proyecto aplica **Arquitectura Hexagonal** separando estrictamente las reglas de negocio de los detalles de infraestructura:

```text
com.jonathan.banco
├── domain
│   ├── model           # Entidades inmutables de dominio (Java puro, sin Spring/JPA)
│   └── exception       # Excepciones de negocio (ReglaNegocioException, RecursoNoEncontradoException)
├── application
│   ├── port
│   │   ├── in          # Casos de uso (ICrearClienteUseCase, ...) y Commands (records)
│   │   └── out         # Puertos de salida (IClienteRepositoryPort, ...) y Strategy de transacciones
│   └── service         # Orquestación transaccional (@Service, @Transactional, inyección por constructor)
└── infrastructure
    └── adapter
        ├── in.web      # Controladores REST, DTOs Request/Response y GlobalExceptionHandler
        └── out.persistence # Entidades JPA (@Entity, @Version), Repositorios Spring Data y Mappers
```

### Patrones y principios aplicados
- **Inmutabilidad y Rich Domain Model**: [`Cliente`](src/main/java/com/jonathan/banco/domain/model/Cliente.java), [`Cuenta`](src/main/java/com/jonathan/banco/domain/model/Cuenta.java) y [`Transaccion`](src/main/java/com/jonathan/banco/domain/model/Transaccion.java) encapsulan sus invariantes (validación de mayoría de edad, saldo no negativo, cancelación solo con saldo cero, cuenta activa para operar).
- **Inyección por constructor (`private final`)**: Sin `@Autowired` en campos, facilitando la inmutabilidad y las pruebas unitarias.
- **Patrón Strategy**: [`ITransaccionStrategy`](src/main/java/com/jonathan/banco/application/port/out/strategy/ITransaccionStrategy.java) desacopla el cálculo de [`ConsignacionStrategy`](src/main/java/com/jonathan/banco/application/port/out/strategy/ConsignacionStrategy.java), [`RetiroStrategy`](src/main/java/com/jonathan/banco/application/port/out/strategy/RetiroStrategy.java) y [`TransferenciaStrategy`](src/main/java/com/jonathan/banco/application/port/out/strategy/TransferenciaStrategy.java) (cumpliendo **Open/Closed Principle**).
- **Control de concurrencia optimista (`@Version`)**: [`CuentaEntity`](src/main/java/com/jonathan/banco/infrastructure/adapter/out/persistence/CuentaEntity.java) evita condiciones de carrera (*lost updates*) en transferencias simultáneas, respondiendo `409 Conflict`.

---

## 🚀 Ejecución Rápida con Podman / Docker Compose

Para levantar PostgreSQL 16 + la API optimizada con un solo comando:

```bash
podman compose up --build -d
# o con Docker:
docker compose up --build -d
```

- **Health Check (Readiness)**: `http://localhost:8080/actuator/health/readiness`
- **API Base URL**: `http://localhost:8080/api`

---

## 🧪 Pruebas Unitarias, de Controladores y Cobertura (JaCoCo)

La suite cuenta con **40 pruebas automatizadas** (JUnit 5, Mockito, `@WebMvcTest` y `MockMvc`) que se ejecutan en memoria sin requerir base de datos activa:

```bash
./mvnw clean verify
```

El reporte interactivo de cobertura se genera en:
`target/site/jacoco/index.html` *(100% de cobertura de líneas en servicios, estrategias y capa web).*

---

## 📌 Endpoints Principales

### 1. Clientes (`/api/clientes`)
- `POST /api/clientes` — Crear cliente (mayor de 18 años, identificación única).
- `GET /api/clientes` — Listar clientes.
- `GET /api/clientes/{id}` — Consultar cliente por ID.
- `PUT /api/clientes/{id}` — Actualizar cliente (actualiza `fechaModificacion`).
- `DELETE /api/clientes/{id}` — Eliminar cliente (bloqueado con `422` si tiene cuentas asociadas).

### 2. Cuentas (`/api/cuentas`)
- `POST /api/cuentas` — Crear cuenta (`AHORROS` prefijo `53`, `CORRIENTE` prefijo `33`, 10 dígitos únicos, activa con saldo `0`).
- `GET /api/cuentas/{id}` — Consultar cuenta por ID.
- `GET /api/cuentas?clienteId={id}` — Listar cuentas de un cliente.
- `PATCH /api/cuentas/{id}/activar` — Activar cuenta.
- `PATCH /api/cuentas/{id}/inactivar` — Inactivar cuenta.
- `PATCH /api/cuentas/{id}/cancelar` — Cancelar cuenta (solo si `saldo == 0`).

### 3. Transacciones (`/api/transacciones`)
- `POST /api/transacciones` — Ejecutar `CONSIGNACION`, `RETIRO` o `TRANSFERENCIA` (atómica con `@Transactional`).
- `GET /api/transacciones?cuentaId={id}` — Historial de movimientos de una cuenta.

---

## ☁️ Despliegue en AWS
Consulta la guía paso a paso en [`docs/despliegue-aws.md`](docs/despliegue-aws.md).
