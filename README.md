# Customer Order Management — Microservices

Two-service Spring Boot solution for a simple customer/order management system, built for a technical assessment.

- [`order-system-service`](order-system-service) — internal service owning MySQL persistence (customers, orders).
- [`order-aggregator-service`](order-aggregator-service) — public-facing service that validates requests and orchestrates calls to the system service.

See [`docs/architecture.md`](docs/architecture.md) for the full design and [`docs/design-decisions.md`](docs/design-decisions.md) for the reasoning behind judgment calls made where the spec was silent.

## Running locally

```bash
docker compose up --build
```

This starts MySQL, `order-system-service` (port 8081) and `order-aggregator-service` (port 8080).

A minimal test UI is available in [`frontend/`](frontend) — serve it with any static server and point it at the aggregator on `http://localhost:8080`.

A Postman collection is available in [`postman/`](postman).
