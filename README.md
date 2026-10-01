# eventservice

Spring Boot 3.5 (Java 25, Maven) service for versioned event ingestion: `POST /api/events` validates a `v1` payload against a JSON Schema (`schemas/event-v1.json`) and persists it with Spring Data JPA. Moved out of `bankingservices`.

- Port `8085`, context path `/event` (base URL `http://localhost:8085/event`)
- Uses the **existing `events` table in `db_example`** (same MySQL as bankingservices). `ddl-auto: none`: the service never creates, alters or drops the table. `db/` has the DDL and sample rows for reference.
- DB settings come from the environment (nothing secret is committed): `EVENTS_DB_PASSWORD` (required), `EVENTS_DB_USERNAME` (default `root`), `EVENTS_DB_URL` (default `jdbc:mysql://localhost:3306/db_example?useSSL=false`).

| Method | Endpoint Path | Description |
| :--- | :--- | :--- |
| `POST` | `/api/events` | `200` saved; `400` schema failure, unsupported `version`, or malformed JSON; `500` unexpected error |

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 25)
mvn test                       # no MySQL needed (mocks + H2)
EVENTS_DB_PASSWORD=... mvn spring-boot:run
curl -s -X POST http://localhost:8085/event/api/events -H "Content-Type: application/json" \
  -d '{"version":"v1","eventId":"evt_123abc","timestamp":"2026-09-17T10:00:00Z","payload":{"userId":"u123","email":"user@example.com"}}'
```
