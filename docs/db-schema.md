# Database Schema

MySQL 8. Managed by Flyway; the authoritative source is
[`order-system-service/src/main/resources/db/migration/V1__init_schema.sql`](../order-system-service/src/main/resources/db/migration/V1__init_schema.sql).
This doc mirrors it for quick reference.

## `customers`

| Column      | Type          | Constraints                  |
|-------------|---------------|-------------------------------|
| id          | BIGINT        | PK, auto-increment            |
| full_name   | VARCHAR(255)  | NOT NULL                      |
| email       | VARCHAR(255)  | NOT NULL                      |
| phone       | VARCHAR(30)   | NOT NULL                      |
| created_at  | DATETIME      | NOT NULL, set by the app      |

## `orders`

| Column       | Type          | Constraints                                 |
|--------------|---------------|-----------------------------------------------|
| id           | BIGINT        | PK, auto-increment                            |
| customer_id  | BIGINT        | NOT NULL, FK → `customers.id`                 |
| product_name | VARCHAR(255)  | NOT NULL                                      |
| quantity     | INT           | NOT NULL (app-validated >= 1)                 |
| price        | DECIMAL(12,2) | NOT NULL (app-validated >= 0)                 |
| status       | VARCHAR(20)   | NOT NULL — one of `CREATED`, `PREPARING`, `FINISHED` (stored as the enum name, not an ordinal, for readability) |
| created_at   | DATETIME      | NOT NULL, set by the app                      |

An index on `orders.customer_id` supports the "get a customer's orders" lookup.

## Relationship

One `customers` row to many `orders` rows, enforced by `fk_orders_customer`.
No cascade delete — the deliverables only require Create/Read/Update, not Delete,
so this was never exercised and no cascade policy was needed.
