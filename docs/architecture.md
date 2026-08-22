# Architecture

## Overview

Two independently deployable Spring Boot services implement a simple customer/order
management system:

```
                     ┌─────────────────────┐
  Client / Frontend  │                      │
  ───────────────►   │  order-aggregator-   │
   HTTP (public)     │  service  (:8080)    │
                     │                      │
                     └──────────┬───────────┘
                                │ WebClient (HTTP)
                                │ X-Internal-Api-Key
                                ▼
                     ┌──────────────────────┐
                     │  order-system-        │
                     │  service  (:8081)     │
                     │                       │
                     └──────────┬────────────┘
                                │ Spring Data JPA
                                ▼
                     ┌──────────────────────┐
                     │      MySQL 8          │
                     └──────────────────────┘
```

## order-system-service

Owns persistence. It is the only component that talks to MySQL. It exposes a small
internal REST API under `/internal/**`, guarded by a shared `X-Internal-Api-Key`
header so that only the aggregator (or another trusted caller with the key) can
reach it — it is not meant to be exposed publicly.

Layers: `entity` → `repository` (Spring Data JPA) → `service` (business logic,
transactions) → `controller` (HTTP). DTOs decouple the wire format from the JPA
entities. A `@RestControllerAdvice` maps domain exceptions to HTTP status codes.

Schema is managed with Flyway (`src/main/resources/db/migration`), not
`hibernate.ddl-auto` — versioned, repeatable, and reviewable migrations. Hibernate's `ddl-auto` is set
to `validate` only, as a safety net that fails fast if the entity mappings and the
Flyway-managed schema ever drift apart.

## order-aggregator-service

The public-facing entry point. It owns request validation (Jakarta Bean Validation
annotations on request DTOs, plus manual enum parsing for order status), talks to
the system service over HTTP via `WebClient`, and normalizes every response —
success or failure — into a single `ApiResponse` envelope so API consumers only
ever need to check one field (`success`) to know how to handle a response.

It never talks to MySQL directly and has no entities — only DTOs, mapped straight
from the system service's JSON responses.

## Request flow

1. Client calls an aggregator endpoint (`/api/v1/**`).
2. Aggregator validates the request body (`@Valid` + Bean Validation annotations;
   order status is parsed against a local enum copy).
3. Aggregator calls the matching system-service endpoint via `WebClient`, attaching
   the internal API key header.
4. System service validates the customer/order exists where relevant, performs the
   DB operation, and returns a DTO (or a 404/400 JSON error).
5. Aggregator maps the system service's response (or error) into an `ApiResponse`
   and returns it to the client.

Both layers log: incoming requests, outgoing calls (aggregator → system service),
and error scenarios — to the console only, via SLF4J/Lombok `@Slf4j`.

## Why two services instead of one

This mirrors a typical gateway/backend split: the aggregator is the layer that
would eventually hold cross-cutting concerns (auth, rate limiting, request
shaping, aggregating multiple downstream calls into one response) without the
system service needing to know about any of it. For this assessment's scope
(one entity relationship, four endpoints) the split is more structure than the
business logic strictly needs, but it's exactly what was asked for.
