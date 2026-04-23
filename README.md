# Smart Campus Sensor and Room Management API

## API Design Overview

This project implements a fully RESTful API for the University's "Smart Campus" initiative. The system manages **Rooms** and **Sensors** (CO2 monitors, occupancy trackers, temperature sensors) deployed across campus buildings using JAX-RS (Jersey) with Apache Tomcat.

### Technology Stack

| Component | Technology |
|-----------|-----------|
| Framework | JAX-RS 2.1 via Jersey 2.41 |
| Server | Apache Tomcat 9.x |
| Build Tool | Apache Maven 3.9+ |
| Data Storage | In-memory ConcurrentHashMap (no database) |
| JSON | Jackson via jersey-media-json-jackson |
| Java Version | JDK 11+ |

### Project Structure

```
SmartCampusAPI/
├── pom.xml
└── src/main/
    ├── java/com/smartcampus/
    │   ├── application/SmartCampusApplication.java   ← @ApplicationPath("/api/v1")
    │   ├── model/Room.java
    │   ├── model/Sensor.java
    │   ├── model/SensorReading.java
    │   ├── model/ErrorResponse.java
    │   ├── storage/DataStore.java                    ← Singleton ConcurrentHashMap store
    │   ├── resource/DiscoveryResource.java           ← GET /api/v1
    │   ├── resource/RoomResource.java                ← /api/v1/rooms
    │   ├── resource/SensorResource.java              ← /api/v1/sensors
    │   ├── resource/SensorReadingResource.java       ← Sub-resource (Part 4)
    │   ├── exception/RoomNotEmptyException.java
    │   ├── exception/RoomNotEmptyExceptionMapper.java
    │   ├── exception/LinkedResourceNotFoundException.java
    │   ├── exception/LinkedResourceNotFoundExceptionMapper.java
    │   ├── exception/SensorUnavailableException.java
    │   ├── exception/SensorUnavailableExceptionMapper.java
    │   ├── exception/GlobalExceptionMapper.java
    │   ├── exception/NotFoundExceptionMapper.java
    │   └── filter/LoggingFilter.java
    └── webapp/WEB-INF/web.xml
```

---

## API Endpoint Table

| Method | Endpoint | Description | Success | Error |
|--------|----------|-------------|---------|-------|
| `GET` | `/api/v1` | Discovery — returns API metadata, version, resource map | 200 OK | — |
| `GET` | `/api/v1/rooms` | List all rooms (full objects) | 200 OK | — |
| `POST` | `/api/v1/rooms` | Create a new room | 201 Created | 400 Bad Request |
| `GET` | `/api/v1/rooms/{roomId}` | Get a specific room by ID | 200 OK | 404 Not Found |
| `DELETE` | `/api/v1/rooms/{roomId}` | Delete a room (blocked if sensors assigned) | 204 No Content | 404 / 409 Conflict |
| `POST` | `/api/v1/sensors` | Register a new sensor (validates roomId exists) | 201 Created | 400 / 422 Unprocessable |
| `GET` | `/api/v1/sensors` | List all sensors | 200 OK | — |
| `GET` | `/api/v1/sensors?type={type}` | Filter sensors by type (e.g. CO2, Temperature) | 200 OK | — |
| `GET` | `/api/v1/sensors/{sensorId}` | Get a specific sensor by ID | 200 OK | 404 Not Found |
| `GET` | `/api/v1/sensors/{sensorId}/readings` | Get full historical readings for a sensor | 200 OK | 404 Not Found |
| `POST` | `/api/v1/sensors/{sensorId}/readings` | Add a new reading (blocked if MAINTENANCE) | 201 Created | 403 Forbidden |

### HTTP Status Codes Used

| Code | Meaning | When Used |
|------|---------|-----------|
| `200` | OK | Successful GET |
| `201` | Created | Successful POST (room, sensor, reading) |
| `204` | No Content | Successful DELETE |
| `400` | Bad Request | Missing required fields |
| `403` | Forbidden | Posting reading to MAINTENANCE sensor |
| `404` | Not Found | Resource does not exist |
| `409` | Conflict | Deleting a room that still has sensors |
| `415` | Unsupported Media Type | Wrong Content-Type sent |
| `422` | Unprocessable Entity | Sensor references a non-existent roomId |
| `500` | Internal Server Error | Unexpected server error (no stack trace exposed) |

---

## Build and Run Instructions

### Prerequisites

- Java JDK 11 or later
- Apache Maven 3.6+
- Apache Tomcat 9.x
- NetBeans IDE 24 (recommended)

### Step 1 — Clone the Repository

```bash
git clone https://github.com/[your-username]/SmartCampusAPI.git
cd SmartCampusAPI
```

### Step 2 — Build with Maven

```bash
mvn clean package
```

This produces `target/SmartCampusAPI.war`.

### Step 3 — Deploy via NetBeans

1. Open **NetBeans as Administrator** (right-click → Run as administrator)
2. **File → Open Project** → select the `SmartCampusAPI` folder
3. Right-click the project → **Clean and Build**
4. Right-click the project → **Run**
5. NetBeans deploys automatically to Tomcat

### Step 4 — Verify

Open your browser and go to:
```
http://localhost:8080/SmartCampusAPI/api/v1
```

You should see the JSON discovery response.

---

## Sample curl Commands

### 1. Discovery Endpoint

```bash
curl -X GET http://localhost:8080/SmartCampusAPI/api/v1 -H "Accept: application/json"
```

**Expected (200 OK):**
```json
{
  "api": "Smart Campus Sensor and Room Management API",
  "version": "1.0",
  "status": "operational",
  "resources": {
    "rooms": "/api/v1/rooms",
    "sensors": "/api/v1/sensors"
  }
}
```

---

### 2. Create a Room

```bash
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d "{\"id\": \"ENG-205\", \"name\": \"Engineering Seminar Room\", \"capacity\": 40}"
```

**Expected (201 Created):**
```json
{
  "id": "ENG-205",
  "name": "Engineering Seminar Room",
  "capacity": 40,
  "sensorIds": []
}
```

---

### 3. List All Rooms

```bash
curl -X GET http://localhost:8080/SmartCampusAPI/api/v1/rooms -H "Accept: application/json"
```

---

### 4. Register a Sensor

```bash
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d "{\"id\": \"TEMP-002\", \"type\": \"Temperature\", \"status\": \"ACTIVE\", \"currentValue\": 21.0, \"roomId\": \"ENG-205\"}"
```

**Expected (201 Created):**
```json
{
  "id": "TEMP-002",
  "type": "Temperature",
  "status": "ACTIVE",
  "currentValue": 21.0,
  "roomId": "ENG-205"
}
```

---

### 5. Filter Sensors by Type

```bash
curl -X GET "http://localhost:8080/SmartCampusAPI/api/v1/sensors?type=CO2" -H "Accept: application/json"
```

---

### 6. Post a Sensor Reading

```bash
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/sensors/TEMP-001/readings \
  -H "Content-Type: application/json" \
  -d "{\"value\": 23.7}"
```

**Expected (201 Created):**
```json
{
  "id": "d4f8a1b2-...",
  "timestamp": 1714000000000,
  "value": 23.7
}
```

---

### 7. Get Reading History

```bash
curl -X GET http://localhost:8080/SmartCampusAPI/api/v1/sensors/TEMP-001/readings -H "Accept: application/json"
```

---

### 8. Delete Room with Sensors — expect 409 Conflict

```bash
curl -X DELETE http://localhost:8080/SmartCampusAPI/api/v1/rooms/LIB-301 -H "Accept: application/json"
```

**Expected (409 Conflict):**
```json
{
  "status": 409,
  "error": "Conflict",
  "message": "Room 'LIB-301' cannot be deleted. It has 1 sensor(s) still assigned: [TEMP-001]",
  "timestamp": 1714000000000
}
```

---

### 9. Register Sensor with Non-Existent Room — expect 422

```bash
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d "{\"id\": \"TEMP-999\", \"type\": \"Temperature\", \"status\": \"ACTIVE\", \"currentValue\": 20.0, \"roomId\": \"GHOST-999\"}"
```

**Expected (422 Unprocessable Entity):**
```json
{
  "status": 422,
  "error": "Unprocessable Entity",
  "message": "Cannot register sensor. Room with ID 'GHOST-999' does not exist.",
  "timestamp": 1714000000000
}
```

---

### 10. Post Reading to MAINTENANCE Sensor — expect 403

```bash
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/sensors/OCC-001/readings \
  -H "Content-Type: application/json" \
  -d "{\"value\": 45}"
```

**Expected (403 Forbidden):**
```json
{
  "status": 403,
  "error": "Forbidden",
  "message": "Sensor 'OCC-001' is currently under MAINTENANCE and cannot accept new readings.",
  "timestamp": 1714000000000
}
```

---

## Pre-seeded Test Data

The API starts with the following data for immediate testing:

| Type | ID | Details |
|------|----|---------|
| Room | `LIB-301` | Library Quiet Study, capacity 50 |
| Room | `LAB-101` | Computer Science Lab, capacity 30 |
| Room | `HALL-A` | Main Hall, capacity 200 |
| Sensor | `TEMP-001` | Temperature, ACTIVE, in LIB-301 |
| Sensor | `CO2-001` | CO2, ACTIVE, in LAB-101 |
| Sensor | `OCC-001` | Occupancy, MAINTENANCE, in HALL-A |

---

## Report: Answers to Coursework Questions

### Part 1.1 — JAX-RS Resource Lifecycle

**Question:** Explain the default lifecycle of a JAX-RS Resource class and its impact on in-memory data management.

By default, JAX-RS creates a new instance of each Resource class for every incoming HTTP request (request-scoped lifecycle). This means resource classes are not singletons — a fresh Java object is instantiated, used to serve a single request, and then eligible for garbage collection. This is the default per-request scope defined by the JAX-RS specification.

This architectural decision has a critical implication for in-memory data management. Because each request receives a brand-new resource instance, any data stored as instance-level fields would be completely re-initialized on every request, resulting in total data loss between API calls.

To prevent this, all shared mutable state must be stored outside the resource class in a centralized singleton. Furthermore, because multiple HTTP requests can arrive and be processed concurrently on different threads, the shared data structures must be thread-safe. This project uses:

- **ConcurrentHashMap** for rooms and sensors — allows concurrent reads and thread-safe writes without blocking.
- **Collections.synchronizedList()** for sensor reading lists — ensures atomic add operations under concurrent access.

Without these synchronization strategies, concurrent requests could corrupt the data store, cause race conditions, or produce inconsistent responses — all critical failures in a production API.

---

### Part 1.2 — HATEOAS

**Question:** Why is HATEOAS considered a hallmark of advanced RESTful design? How does it benefit client developers?

HATEOAS is the principle that API responses should include hyperlinks to related resources and available actions, allowing clients to navigate the API dynamically rather than relying on hard-coded URLs or static external documentation.

For example, a response to GET /api/v1 in this project returns not just data, but a complete resource map: rooms → /api/v1/rooms, sensors → /api/v1/sensors, and all available actions. This transforms the API into a self-documenting, self-navigating system.

The benefits over static documentation are significant:

- **Decoupling:** Clients follow links from responses rather than constructing URLs themselves, so server-side URL restructuring does not break existing clients.
- **Discoverability:** New developers can explore the entire API starting from a single entry point without consulting external docs.
- **Always current:** Static documentation becomes stale as APIs evolve; hypermedia responses always reflect the current, actual state of the API.
- **Reduced integration errors:** Clients cannot accidentally call a non-existent endpoint if they always follow server-provided links.

---

### Part 2.1 — Room Resource Implementation

**Question:** What are the implications of returning only IDs versus full room objects in a list response?

When returning a collection of rooms from GET /api/v1/rooms, the API designer must choose between returning only identifiers or returning complete room objects with all fields.

Returning only IDs minimizes payload size and reduces serialization overhead. However, it forces clients to make N additional GET requests to retrieve the details of each room — the classic **N+1 problem**. For a campus with 500 rooms, this means 501 HTTP round-trips, dramatically increasing total latency and server load.

Returning full objects uses more bandwidth per initial call, but eliminates the N+1 problem entirely. The client receives everything it needs in a single response, which is far more efficient in practice. For read-heavy APIs like this one, where clients frequently need room details, returning full objects with server-side pagination is the industry-standard approach that correctly balances network bandwidth against client-side processing efficiency.

---

### Part 2.2 — Room Deletion and Safety Logic

**Question:** Is the DELETE operation idempotent in your implementation? Justify in detail.

Yes, DELETE is idempotent in this implementation. Idempotency is the property whereby making the same request multiple times produces the same server state as making it exactly once — regardless of how many times the operation is repeated.

In this API, the first DELETE /api/v1/rooms/ENG-205 successfully removes the room from the ConcurrentHashMap data store and returns HTTP 204 No Content. If the identical DELETE request is sent a second time, the room no longer exists and the server returns HTTP 404 Not Found.

Critically, however, the server's state is identical after both calls: the room is absent in both cases. The response status code differs, but idempotency is defined in terms of server-side state changes, not response codes. This is explicitly consistent with RFC 7231, which defines idempotency as: "the intended effect on the server of multiple identical requests is the same as the effect for a single such request." Therefore, DELETE is fully idempotent in this implementation.

---

### Part 3.1 — Sensor Resource and Integrity

**Question:** What happens if a client sends data in a format other than application/json?

The `@Consumes(MediaType.APPLICATION_JSON)` annotation on the POST /sensors method declares a contract to the JAX-RS runtime: this endpoint exclusively accepts request bodies with Content-Type: application/json.

If a client sends a request with Content-Type: text/plain or Content-Type: application/xml, the JAX-RS framework intercepts the request before the resource method is ever invoked and automatically returns **HTTP 415 Unsupported Media Type**. No custom code is required in the resource method to handle this case.

This behaviour provides two key benefits: it enforces the API contract at the framework level, and it cleanly separates protocol-level content negotiation from business logic. The resource method body only executes when it receives properly typed, parseable JSON — maintaining a clean single-responsibility design.

---

### Part 3.2 — Filtered Retrieval and Search

**Question:** Why is @QueryParam superior to path-based filtering for collection searches?

Using `@QueryParam` for filtering is architecturally superior to embedding the filter in the path for several well-established reasons in REST design:

- **Semantic correctness:** URL path segments should identify specific resources or resource collections. Query parameters are semantically designated for filtering, searching, sorting, and pagination operations on those collections.
- **Optionality:** A query parameter can be omitted entirely — GET /sensors returns all sensors, while GET /sensors?type=CO2 returns filtered results. A path-based design requires a separate route definition for the unfiltered case.
- **Composability:** Multiple query parameters compose naturally (e.g. ?type=CO2&status=ACTIVE). Path-based filtering with multiple criteria produces deeply nested and unreadable URL structures.
- **HTTP caching:** Query-parameterised URLs are better handled by standard HTTP caching infrastructure and CDN layers, improving performance at scale.
- **Industry standard:** REST API guidelines from Google, Microsoft, and AWS all specify query parameters as the correct mechanism for collection filtering.

---

### Part 4.1 — The Sub-Resource Locator Pattern

**Question:** Discuss the architectural benefits of the Sub-Resource Locator pattern.

The Sub-Resource Locator pattern involves a resource method that does not handle an HTTP request directly, but instead returns an instance of another resource class that will handle requests to a nested sub-path. In this project, SensorResource's `getReadingsResource()` method returns a SensorReadingResource instance for the path `{sensorId}/readings` — it does not itself process GET or POST requests on that path.

The architectural benefits compared to a monolithic controller approach are substantial:

- **Single Responsibility Principle:** Each class has one clearly defined responsibility. SensorResource manages sensors; SensorReadingResource manages reading history. Neither class needs to know the internal workings of the other.
- **Maintainability:** In large APIs with dozens of nested paths, a single monolithic controller can grow to thousands of lines, becoming extremely difficult to understand, maintain, and extend without introducing regressions.
- **Independent testability:** SensorReadingResource can be unit-tested in complete isolation, injected with a mock DataStore, without needing to instantiate or configure SensorResource at all.
- **Team scalability:** In a development team, separate resource classes allow parallel development with minimal merge conflicts. One developer can work on reading management independently of another working on sensor management.
- **Extensibility:** New operations on readings (e.g., aggregations, exports) can be added to SensorReadingResource without touching SensorResource, following the Open/Closed Principle.

---

### Part 5.2 — Dependency Validation (422 Unprocessable Entity)

**Question:** Why is HTTP 422 more semantically accurate than 404 for a missing referenced resource in a payload?

When a client POSTs a new sensor with a roomId of "GHOST-999" that does not exist in the system, the correct response is HTTP 422 Unprocessable Entity, not HTTP 404 Not Found. The distinction is semantically critical:

**HTTP 404 Not Found** communicates that the request URI itself could not be resolved — that the endpoint /api/v1/sensors does not exist on the server. This is factually incorrect; the endpoint is fully operational and reachable.

**HTTP 422 Unprocessable Entity** communicates something precise and different: the request URL is valid, the HTTP method is correct, the Content-Type is accepted, and the JSON body is syntactically well-formed — but the semantic content of the payload is logically invalid. Specifically, a field within the payload references an entity (roomId) that does not exist in the system, making it impossible to fulfil the request as submitted.

This distinction gives the client developer actionable, accurate feedback: the problem is inside the request body, not in the URL. The client knows to correct their payload data, not their endpoint URL. This precision is a hallmark of a well-designed, developer-friendly API.

---

### Part 5.4 — The Global Safety Net

**Question:** What specific information could an attacker gather from exposed Java stack traces?

Exposing raw Java stack traces to external API consumers is a serious information disclosure vulnerability (CWE-209). An attacker can extract multiple categories of actionable intelligence from a single stack trace:

- **Internal architecture mapping:** Fully-qualified class names reveal the package structure and architectural layers, enabling targeted class-level attacks.
- **Library fingerprinting:** JAR file names and versions in the trace allow attackers to cross-reference public CVE databases for known, exploitable vulnerabilities in those exact versions.
- **File system path disclosure:** Absolute paths reveal the deployment environment, enabling path traversal and directory enumeration attacks.
- **Business logic exposure:** The call stack reveals the exact sequence of method calls, allowing attackers to reverse-engineer application flow, identify validation gaps, and craft inputs that exploit specific code paths.

This project's GlobalExceptionMapper intercepts all Throwable instances, logs the complete stack trace server-side (accessible only to administrators), and returns only a generic HTTP 500 Internal Server Error with a non-revealing message to the client. This implements the security principle of **Defence in Depth** — minimising information leakage at every layer.

---

### Part 5.5 — API Request and Response Logging Filters

**Question:** Why use JAX-RS filters for logging rather than inserting Logger.info() in every resource method?

Manually inserting Logger.info() statements inside every resource method is an anti-pattern for several architectural reasons:

- **DRY Violation:** Identical or near-identical logging code repeated across dozens of methods creates maintenance overhead. Changing the log format requires editing every method individually.
- **Tight coupling:** Business logic becomes entangled with infrastructure concerns (logging), violating the Single Responsibility Principle and making resource methods harder to read and test.
- **Inconsistency risk:** A developer adding a new endpoint might forget to add logging, creating gaps in observability.

JAX-RS ContainerRequestFilter and ContainerResponseFilter implement the **Aspect-Oriented Programming** pattern — cross-cutting concerns are encapsulated in one place and applied universally to all endpoints, past and future, without any modification to resource methods.

The benefits are concrete:

- **Zero-touch coverage:** Every new endpoint automatically inherits logging, authentication checks, CORS headers, or any other filter logic without a single line of additional code in the resource class.
- **Centralised maintenance:** Log format, log level, or logging destination changes require editing exactly one class.
- **Clean separation:** Resource methods contain only business logic, making them easier to read, test, and reason about.
- **Pluggability:** Filters can be enabled, disabled, or replaced independently of the resources they intercept.
