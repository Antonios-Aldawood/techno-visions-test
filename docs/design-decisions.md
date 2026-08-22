# Design Decisions

The assessment doc specifies entities, endpoints, and a tech stack, but leaves
several concrete choices open. This records what was decided and why, for
anywhere the reasoning isn't obvious from the code itself.

## Order status enum: CREATED, PREPARING, FINISHED (no RECEIVED)

The doc lists Created → Preparing → Finished explicitly and says nothing about a
delivery/transportation stage. Since there's no transportation-management concern
in scope, a `RECEIVED` status would represent a lifecycle stage the system has no
way to reach or observe. Left out; trivial to add later as a fourth enum constant
if the scope grows to include fulfillment/delivery tracking.

## No status-transition state machine

`PUT /orders/{id}/status` accepts any of the three valid enum values regardless of
the order's current status — CREATED can jump straight to FINISHED. The doc
doesn't ask for enforced forward-only transitions, and a real state machine (valid
transition table, guard logic, dedicated error codes per illegal transition) is
more machinery than a three-state enum warrants here. Easy to layer on later if
needed.

## Quantity must be >= 1, not >= 0

Quantity has to be validated. An order line for zero units isn't a meaningful order. Enforced
`>= 1` instead. If this reading is wrong, it's a one-line change
(`@Min(1)` → `@Min(0)` in both `CreateOrderRequest` classes).

## Price allows 0, not just > 0

Validation only that it isn't under 0, thus a $0 line item
(e.g. a promotional item) is a plausible business case, unlike zero quantity.

## Internal API key (X-Internal-Api-Key)

The doc doesn't require auth between the two services. A single shared-secret
header check was added anyway: it's a few lines (one servlet filter, one outgoing
header), and it establishes that `/internal/**` isn't meant to be reachable by
arbitrary callers even though nothing else (network segmentation, real
authn/authz) is in place to enforce that in this local/assessment setup. Not
intended to read as production-grade security — no rotation, no per-caller
identity, no expiry, but an important clarification that intenral service must be proteceted securely.

## Standard response envelope in the aggregator only

`{ success, data, error, timestamp }` is used for every aggregator response,
success or failure, so a client only ever needs to check one field. The system
service does **not** use this envelope — it returns plain resource JSON on
success and a small `{ code, message, timestamp }` body on error, since it's an
internal API consumed only by the aggregator, not a public contract that needs a
polished shape.

## Duplicated OrderStatus enum, not a shared module

The aggregator keeps its own copy of the three-value `OrderStatus` enum rather
than depending on a shared library between the two services. A shared module
would couple the two services' build/release cycles together, which cuts against
having them be independently deployable — a bigger cost than keeping three enum
constants in sync by hand across two files.

## Validation: Jakarta Bean Validation annotations, not hand-rolled checks

Field-level rules (blank checks, email format, phone pattern, numeric bounds) are
expressed as `@NotBlank`/`@Email`/`@Pattern`/`@Min`/`@DecimalMin` on the request
DTOs rather than manual `if` chains. This is the idiomatic Spring approach, maps
directly to the doc's own "Validation" tech-stack bullet, and is less code than
hand-rolled checks would be. The one thing bean validation annotations don't
express cleanly — "is this string a legal enum value, with a clear error message
listing the legal ones" — is handled by a small manual parse method
(`OrderStatus.parse`) instead.

## Flyway over `hibernate.ddl-auto`

`ddl-auto` can generate/update a schema at runtime, but it isn't a real migration
tool: no version history, no rollback, and it's explicitly discouraged for
anything beyond quick local prototyping. Flyway's versioned SQL files
(`V1__init_schema.sql`) are the code-first migrations, and they double as the "SQL schema or migrations" deliverable.
`ddl-auto` is still set, but only to `validate` — it fails fast at startup if the
JPA entity mappings and the Flyway-managed schema ever disagree, without ever
generating or altering DDL itself.

## Monorepo now, split into two repos at submission

The doc asks for "two Git repositories link." Building both services as sibling
folders in one repository (each with its own `pom.xml`, fully independent build)
was faster to develop and verify together (one `docker compose up` brings up the
whole stack). At submission time, each service folder is split out into its own
repository with full history via `git subtree split`, so the end deliverable
matches the requirement.
