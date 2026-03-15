# mbook-quarkus

User, device, and profile API for the Cinemas booking platform. Runs on port **8888** under context path `/mbook-1`.

## What it does

- **User profile** — `GET /rest/user/{user}/{token1}` returns uuid, email, and profilePicture. Protected by `CiphertextFilter` and `CookieFilter`.
- **Device management** — `GET /rest/device/{uuid}` lists all devices registered to a user.
- **Username/email availability** — `GET /rest/newuser/{name}` and `GET /rest/newemail/{email}` for registration validation.
- **News events** — `GET /rest/news/{user}` returns pending notification events for a user.
- **Realtime notifications** — Kafka consumer (`ios-users-notifications` topic) broadcasts user-state changes via WebSocket at `/ws`.

## Architecture

```
dalogin (ServiceClient)
  │  forwards X-Token, Ciphertext, XSRF-TOKEN, TIME_, uuid
  ▼
mbook (:8888, /mbook-1)
  ├── CiphertextFilter ── verifies Ciphertext == token2 header
  ├── CookieFilter ── reconstructs + validates XSRF-TOKEN
  ├── BookController ── /rest/user, /rest/device, /rest/news
  ├── DAO (CDI @RequestScoped) ── Hibernate/JPA queries
  └── KafkaListener → WebSocketServer.broadcastMessage()
           ▼
       MySQL login_
```

## REST endpoints

| Path | Method | Auth | Description |
|------|--------|------|-------------|
| `/rest/user/{user}/{token1}` | GET | Ciphertext + Cookie | Get user profile (uuid, email, profilePicture) |
| `/rest/device/{uuid}` | GET | None | List user's devices |
| `/rest/device/test` | GET | None | Health check (returns "Device not found") |
| `/rest/news/{user}` | GET | None | Get pending notification events |
| `/rest/newuser/{name}` | GET | None | Check username availability |
| `/rest/newemail/{email}` | GET | None | Check email availability |

## Security filters

| Filter | Applies to | Checks |
|--------|-----------|--------|
| `CiphertextFilter` | paths containing "user" | `Ciphertext` header must equal `token2` header |
| `CookieFilter` | paths containing "user" | Reconstructs XSRF token from `TIME_` + `token2`, validates against `XSRF-TOKEN` cookie |

## Database

Uses MySQL schema **`login_`** via Quarkus-managed Hibernate/JPA.

| Entity | Table | Key fields |
|--------|-------|------------|
| `Logins` | logins | id, user, uuid, email, profilePicture |
| `Devices` | devices | id, deviceId, uuid |
| `Tokens` | Tokens | id, deviceId, token1, token2, TIME_ |

## Kafka & WebSocket

- **Consumer**: `KafkaListener` subscribes to `ios-users-notifications`, broadcasts messages to all WebSocket clients.
- **Consumer**: `KafkaMoviesListener` subscribes to `ios-movies-notifications2`, broadcasts movie updates.
- **WebSocket**: `@ServerEndpoint("/ws")` — Apache proxies `/mbook-1/ws`.

## Environment variables

| Variable | Default | Description |
|----------|---------|-------------|
| `DB_URL` | `jdbc:mysql://localhost:3306/login_` | JDBC URL |
| `BOOTSTRAP_URL` | `localhost:9092` | Kafka bootstrap server |

## Build & Run

```bash
./mvnw quarkus:dev                   # dev mode on port 8888
./mvnw package -DskipTests           # package for container
podman build -t mbook-quarkus:local .
```

## Part of the Cinemas platform

| Service | Repo | Role |
|---------|------|------|
| dalogin-quarkus | [igeorge0902/dalogin-quarkus](https://github.com/igeorge0902/dalogin-quarkus) | Auth gateway |
| **mbook-quarkus** | this repo | User/device API |
| mbooks-quarkus | [igeorge0902/mbooks-quarkus](https://github.com/igeorge0902/mbooks-quarkus) | Movie/booking/payment API |
| simple-service-webapp-quarkus | [igeorge0902/simple-service-webapp-quarkus](https://github.com/igeorge0902/simple-service-webapp-quarkus) | Image server |
| k8infra | [igeorge0902/k8infra](https://github.com/igeorge0902/k8infra) | Kubernetes manifests, SQL fixes, deploy runbook |
