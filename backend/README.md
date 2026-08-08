# resilichain-api

Spring Boot backend for ResiliChain Twin (Week 1 scope: network domain entities + JWT auth).

## Run locally

```
docker compose up -d          # from repo root — starts Postgres
cd backend
./mvnw spring-boot:run        # from backend/ — starts the API on :8080
```

`GET /health` should return `{"status":"UP"}` once both are running.

## Seeded dev admin user

Created by the `V2__seed_admin_user.sql` Flyway migration. **Dev only — do not reuse in any real deployment.**

- email: `admin@resilichain.com`
- password: `ChangeMe123!`

```
curl -X POST localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@resilichain.com","password":"ChangeMe123!"}'
```

## Tests

```
./mvnw test
```

Domain entity invariants run as plain JUnit 5 (no Spring context). `NetworkNodeInheritanceMappingTest` and `AuthControllerTest` boot a Spring context against an in-memory H2 database (Flyway disabled, `ddl-auto=create-drop`) — they do not exercise the Postgres-specific Flyway migrations in `src/main/resources/db/migration/`, which only run against the real Postgres instance started via `docker compose up`.
