# TinyURL Service

A TinyURL-like URL shortening service built with **Java 21, Spring Boot, PostgreSQL, and Apache ZooKeeper**. The project explores backend fundamentals and distributed-systems concepts such as Base62 encoding, ID range allocation, optimistic concurrency, and thread-safe ID generation.

## Features

- Create short URLs from long URLs.
- Generate compact short codes using **Base62 encoding**.
- Persist URL mappings in **PostgreSQL**.
- Use **ZooKeeper** to allocate ID ranges instead of coordinating with ZooKeeper for every request.
- Use ZooKeeper's versioning mechanism and optimistic concurrency to avoid allocating overlapping ranges.
- Generate IDs locally within each application instance using a synchronized counter.
- Redirect short URLs to their original destinations with HTTP `302`.
- Validate incoming URL requests.

## Tech Stack

- Java 21
- Spring Boot
- Spring Web
- Spring Data JPA / Hibernate
- PostgreSQL 16
- Apache Curator / ZooKeeper 3.9
- Maven
- Docker Compose

## How It Works

### Create a short URL

1. The client sends a long URL to `POST /api/urls`.
2. The service checks whether that long URL already has a mapping.
3. The counter service obtains a range of IDs from ZooKeeper when its local range is exhausted.
4. The numeric ID is converted into a short code using Base62.
5. The mapping is saved in PostgreSQL.
6. The API returns the short code and short URL.

### Redirect

1. The client requests `GET /{shortCode}`.
2. The service looks up the short code in PostgreSQL.
3. The API responds with HTTP `302` and a `Location` header containing the original URL.

### Distributed ID range allocation

Rather than requesting an ID from ZooKeeper for every URL, an application instance reserves a range and consumes IDs locally.

For example, with a range size of `1000`:

```text
Instance A → IDs 0–999
Instance B → IDs 1000–1999
Instance C → IDs 2000–2999
```

ZooKeeper stores the shared counter. The service reads the counter and its version, then updates the counter only if the version has not changed. If another instance updates it first, the allocation is retried. This prevents two instances from successfully reserving the same range.

Within each application instance, synchronization protects local ID consumption from concurrent requests.

> The ranges above are illustrative. Multi-instance behavior should be verified by running multiple application instances against the same ZooKeeper service.

## Architecture

```text
                     ┌────────────────────┐
                     │       Client       │
                     └─────────┬──────────┘
                               │
                     POST /api/urls
                     GET /{shortCode}
                               │
                     ┌─────────▼──────────┐
                     │  Spring Boot App   │
                     │                    │
                     │ REST Controller    │
                     │ Service Layer      │
                     │ Counter Service    │
                     │ Base62 Encoder     │
                     └──────┬───────┬─────┘
                            │       │
                  ID ranges │       │ URL mappings
                            │       │
                  ┌─────────▼─┐   ┌─▼────────────┐
                  │ ZooKeeper │   │ PostgreSQL   │
                  │ Shared ID │   │ URL mapping  │
                  │ counter   │   │ persistence  │
                  └───────────┘   └──────────────┘
```

## API

### 1. Create a short URL

**Request**

```http
POST /api/urls
Content-Type: application/json
```

```json
{
  "longUrl": "https://www.google.com"
}
```

**Example response**

```json
{
  "shortCode": "0",
  "shortUrl": "http://localhost:8080/0"
}
```

The returned code depends on the current counter state and existing mappings.

**cURL**

```bash
curl -X POST "http://localhost:8080/api/urls" \
  -H "Content-Type: application/json" \
  -d '{"longUrl":"https://www.google.com"}'
```

### 2. Redirect to the original URL

```http
GET /{shortCode}
```

Example:

```bash
curl -i "http://localhost:8080/0"
```

A successful lookup returns `302 Found` with a `Location` header pointing to the original URL. Browsers follow the redirect automatically. To inspect the response in Postman, disable automatic redirect-following.

## Run Locally

### Prerequisites

- Java 21
- Maven
- Docker and Docker Compose

### 1. Start PostgreSQL and ZooKeeper

The following is a minimal `docker-compose.yml` example:

```yaml
services:
  postgres:
    image: postgres:16
    container_name: tinyurl-postgres
    environment:
      POSTGRES_DB: tinyurl
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5432:5432"

  zookeeper:
    image: zookeeper:3.9
    container_name: tinyurl-zookeeper
    ports:
      - "2181:2181"
```

Start the services:

```bash
docker compose up -d
```

### 2. Configure the application

Configure the PostgreSQL connection and ZooKeeper connection in `src/main/resources/application.properties` to match your local setup.

Example PostgreSQL settings:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/tinyurl
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.jpa.hibernate.ddl-auto=update
```

Ensure your ZooKeeper/Curator configuration points to:

```text
localhost:2181
```

The exact ZooKeeper properties depend on how the Curator client is configured in the project.

### 3. Run the application

From the project root:

```bash
./mvnw spring-boot:run
```

If the Maven wrapper is not included, use:

```bash
mvn spring-boot:run
```

The application should be available at `http://localhost:8080`.

## Database Schema

The URL mapping table stores the numeric ID, short code, original URL, and creation time.

```sql
CREATE TABLE url_mapping (
    id BIGINT PRIMARY KEY,
    short_code VARCHAR(20) UNIQUE NOT NULL,
    long_url TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL
);
```

The ID is supplied by the application's ZooKeeper-backed counter service rather than generated by the database.

## Run Tests

```bash
./mvnw test
```

Or, without the Maven wrapper:

```bash
mvn test
```

## Current Scope

Implemented:
- URL creation and lookup
- Base62 encoding
- PostgreSQL persistence
- ZooKeeper-backed ID range allocation
- Optimistic concurrency for shared counter updates
- Thread-safe local ID generation
- HTTP redirects and request validation

Potential next steps:
- Redis caching for frequently accessed URLs
- Kafka-based click analytics
- Multi-instance integration and concurrency tests
- Load testing, metrics, and observability
- Dockerizing the application itself

These are future enhancements, not part of the current implementation unless added separately.

## Learning Goals

This project is a practical exploration of how a seemingly simple URL shortener can introduce concepts such as:

- REST API design
- Data persistence and uniqueness constraints
- Base conversion and compact identifiers
- Distributed coordination
- Optimistic concurrency control
- Reducing coordination overhead through ID range allocation
- Thread safety within an application instance

---

**Built with Java, Spring Boot, PostgreSQL, and ZooKeeper.**
