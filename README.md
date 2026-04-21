# Smart Campus Sensor & Room Management API

**Module:** 5COSC022W – Client-Server Architectures  
**Student:** [Your Name]  
**Student ID:** [Your ID]

---

## API Design Overview

This project implements a fully RESTful API for the University's "Smart Campus" initiative. The system manages **Rooms** and **Sensors** (CO2 monitors, occupancy trackers, temperature sensors) deployed across campus buildings.

### Architecture

- **Framework:** JAX-RS 2.1 via Jersey 2.41
- **Server:** Apache Tomcat 9.x
- **Build Tool:** Apache Maven
- **Data Storage:** In-memory `ConcurrentHashMap` (no database)
- **JSON:** Jackson via `jersey-media-json-jackson`

### Resource Hierarchy

```
/api/v1                              ← Discovery endpoint
/api/v1/rooms                        ← Room collection
/api/v1/rooms/{roomId}               ← Individual room
/api/v1/sensors                      ← Sensor collection
/api/v1/sensors?type={type}          ← Filtered sensors
/api/v1/sensors/{sensorId}           ← Individual sensor
/api/v1/sensors/{sensorId}/readings  ← Sub-resource: historical readings
```

### Core Data Models

| Model | Key Fields |
|-------|-----------|
| `Room` | id, name, capacity, sensorIds[] |
| `Sensor` | id, type, status, currentValue, roomId |
| `SensorReading` | id, timestamp, value |

---

## Build & Run Instructions

### Prerequisites

- Java JDK 11 or later
- Apache Maven 3.6+
- Apache Tomcat 9.x
- NetBeans IDE (recommended) or any IDE with Maven support

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

### Step 3 — Deploy to Tomcat

**Option A — NetBeans (Recommended):**
1. Open the project in NetBeans
2. Right-click the project → **Run**
3. NetBeans will deploy to the configured Tomcat instance automatically

**Option B — Manual Tomcat Deploy:**
1. Copy `target/SmartCampusAPI.war` to `$TOMCAT_HOME/webapps/`
2. Start Tomcat: `$TOMCAT_HOME/bin/startup.sh` (Linux/Mac) or `startup.bat` (Windows)

### Step 4 — Verify the Server is Running

```bash
curl http://localhost:8080/SmartCampusAPI/api/v1
```

Expected: JSON metadata response with API version and resource map.

---

## Sample curl Commands

### 1. Discovery Endpoint — GET /api/v1

```bash
curl -X GET http://localhost:8080/SmartCampusAPI/api/v1 \
  -H "Accept: application/json"
```

**Expected Response (200 OK):**
```json
{
  "api": "Smart Campus Sensor & Room Management API",
  "version": "1.0",
  "status": "operational",
  "resources": {
    "rooms": "/api/v1/rooms",
    "sensors": "/api/v1/sensors",
    "sensorReadings": "/api/v1/sensors/{sensorId}/readings"
  }
}
```

---

### 2. Create a Room — POST /api/v1/rooms

```bash
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{
    "id": "ENG-205",
    "name": "Engineering Seminar Room",
    "capacity": 40
  }'
```

**Expected Response (201 Created):**
```json
{
  "id": "ENG-205",
  "name": "Engineering Seminar Room",
  "capacity": 40,
  "sensorIds": []
}
```

---

### 3. List All Rooms — GET /api/v1/rooms

```bash
curl -X GET http://localhost:8080/SmartCampusAPI/api/v1/rooms \
  -H "Accept: application/json"
```

**Expected Response (200 OK):**
```json
[
  { "id": "LIB-301", "name": "Library Quiet Study", "capacity": 50, "sensorIds": ["TEMP-001"] },
  { "id": "LAB-101", "name": "Computer Science Lab", "capacity": 30, "sensorIds": ["CO2-001"] },
  { "id": "HALL-A",  "name": "Main Hall",            "capacity": 200, "sensorIds": ["OCC-001"] }
]
```

---

### 4. Register a Sensor — POST /api/v1/sensors

```bash
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{
    "id": "TEMP-002",
    "type": "Temperature",
    "status": "ACTIVE",
    "currentValue": 21.0,
    "roomId": "ENG-205"
  }'
```

**Expected Response (201 Created):**
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

### 5. Filter Sensors by Type — GET /api/v1/sensors?type=CO2

```bash
curl -X GET "http://localhost:8080/SmartCampusAPI/api/v1/sensors?type=CO2" \
  -H "Accept: application/json"
```

**Expected Response (200 OK):**
```json
[
  {
    "id": "CO2-001",
    "type": "CO2",
    "status": "ACTIVE",
    "currentValue": 400.0,
    "roomId": "LAB-101"
  }
]
```

---

### 6. Post a Sensor Reading — POST /api/v1/sensors/{sensorId}/readings

```bash
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/sensors/TEMP-001/readings \
  -H "Content-Type: application/json" \
  -d '{
    "value": 23.7
  }'
```

**Expected Response (201 Created):**
```json
{
  "id": "d4f8a1b2-...",
  "timestamp": 1714000000000,
  "value": 23.7
}
```

---

### 7. Get Reading History — GET /api/v1/sensors/{sensorId}/readings

```bash
curl -X GET http://localhost:8080/SmartCampusAPI/api/v1/sensors/TEMP-001/readings \
  -H "Accept: application/json"
```

---

### 8. Delete a Room (with sensors — expect 409 Conflict)

```bash
curl -X DELETE http://localhost:8080/SmartCampusAPI/api/v1/rooms/LIB-301 \
  -H "Accept: application/json"
```

**Expected Response (409 Conflict):**
```json
{
  "status": 409,
  "error": "Conflict",
  "message": "Room 'LIB-301' cannot be deleted. It has 1 sensor(s) still assigned: [TEMP-001]",
  "timestamp": 1714000000000
}
```

---

### 9. Register a Sensor with Non-Existent Room (expect 422)

```bash
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{
    "id": "TEMP-999",
    "type": "Temperature",
    "status": "ACTIVE",
    "currentValue": 20.0,
    "roomId": "GHOST-999"
  }'
```

**Expected Response (422 Unprocessable Entity):**
```json
{
  "status": 422,
  "error": "Unprocessable Entity",
  "message": "Cannot register sensor. Room with ID 'GHOST-999' does not exist.",
  "timestamp": 1714000000000
}
```

---

### 10. Post Reading to MAINTENANCE Sensor (expect 403)

```bash
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/sensors/OCC-001/readings \
  -H "Content-Type: application/json" \
  -d '{ "value": 45 }'
```

**Expected Response (403 Forbidden):**
```json
{
  "status": 403,
  "error": "Forbidden",
  "message": "Sensor 'OCC-001' is currently under MAINTENANCE and cannot accept new readings.",
  "timestamp": 1714000000000
}
```

---

## Report: Answers to Questions

### Part 1.1 — JAX-RS Resource Lifecycle

By default, JAX-RS creates a **new instance of each Resource class for every incoming HTTP request** (request-scoped lifecycle). This means resource classes are NOT singletons — a fresh object is constructed, used to serve the request, and then garbage-collected.

**Impact on in-memory data management:**  
Because each request gets a new resource instance, any instance-level fields (e.g., `private Map<String, Room> rooms = new HashMap<>()`) would be re-initialised on every request, causing total data loss between calls. To prevent this, all shared data must be stored in a **centralised singleton** (`DataStore.getInstance()`). Furthermore, since multiple requests can arrive concurrently on different threads, the collections must be thread-safe. This project uses `ConcurrentHashMap` instead of `HashMap` and `Collections.synchronizedList()` for reading lists to prevent race conditions and data corruption under concurrent access.

---

### Part 1.2 — HATEOAS

**Hypermedia as the Engine of Application State (HATEOAS)** means that API responses include links to related resources and available actions, rather than forcing clients to construct URLs from static documentation. For example, a room response might include a link to `/api/v1/rooms/LIB-301/sensors`.

**Benefits over static documentation:**  
Clients become self-navigating — they follow links rather than hard-coding URLs. This decouples the client from the server's URL structure, so the server can evolve its URL design without breaking clients. It also reduces onboarding time for new developers, as the API itself explains what can be done next. Static docs become stale; hypermedia responses are always current.

---

### Part 2.1 — ID-only vs. Full Object Returns

Returning only IDs (e.g., `["LIB-301", "LAB-101"]`) minimises payload size and is faster to serialise, but it forces clients to make N additional requests to retrieve details — the classic "N+1 problem". This dramatically increases latency in large datasets.

Returning full room objects in a single response uses more bandwidth per call, but eliminates the N+1 problem and reduces total round-trips. For a campus-wide API with hundreds of rooms, pagination with full objects is the preferred industry approach, balancing completeness with network efficiency.

---

### Part 2.2 — DELETE Idempotency

**Yes, DELETE is idempotent** in this implementation. Idempotency means that making the same request multiple times produces the same server state as making it once. After the first DELETE on a room, the room is removed. If the same DELETE is sent again, the server returns `404 Not Found` — but the server's *state* is identical: the room is still absent. The response code differs (204 vs 404), but idempotency is about state, not response codes. This is consistent with RFC 7231.

---

### Part 3.1 — @Consumes and Content-Type Mismatches

The `@Consumes(MediaType.APPLICATION_JSON)` annotation tells JAX-RS that this endpoint only accepts `application/json` request bodies. If a client sends `Content-Type: text/plain` or `Content-Type: application/xml`, JAX-RS automatically returns **HTTP 415 Unsupported Media Type** before the method body is even executed. The framework handles this entirely at the routing layer, meaning no manual content-type checking is needed inside the method.

---

### Part 3.2 — @QueryParam vs. Path-Based Filtering

Using `@QueryParam` (e.g., `GET /sensors?type=CO2`) is superior for filtering because:
- **Semantic correctness:** Path segments should identify resources; query parameters should filter or search them.
- **Optionality:** A query param can be omitted entirely (`GET /sensors` returns all). With a path param (`/sensors/type/CO2`), the type becomes mandatory and a separate route is needed for unfiltered access.
- **Multiple filters:** Query params compose naturally (`?type=CO2&status=ACTIVE`). Path-based filters become unwieldy with multiple criteria.
- **Caching:** Query-parameterised URLs are better supported by HTTP caching infrastructure.

---

### Part 4.1 — Sub-Resource Locator Pattern

The Sub-Resource Locator pattern delegates responsibility for a nested path to a dedicated class instead of handling it all in one controller. `SensorResource` returns a `SensorReadingResource` instance for the `{sensorId}/readings` path, rather than defining all reading operations inline.

**Benefits:**  
Each class has a single, well-defined responsibility (Single Responsibility Principle). In large APIs with dozens of nested paths, a monolithic controller becomes unmaintainable, difficult to test, and a merge conflict nightmare in team environments. The locator pattern enables independent testing of `SensorReadingResource`, cleaner code organisation, and allows each sub-resource to evolve independently without touching the parent class.

---

### Part 5.2 — Why 422 over 404 for Missing roomId

When a client POSTs a sensor with a `roomId` of `"GHOST-999"`:
- **404 Not Found** would imply the *URL itself* (`/api/v1/sensors`) was not found — which is wrong; the endpoint exists.
- **422 Unprocessable Entity** correctly communicates that the request was syntactically valid JSON sent to a valid URL, but the *semantic content* is logically invalid — the referenced entity (`roomId`) does not exist in the system.

422 provides precise, actionable feedback: the client knows to fix their payload, not their URL.

---

### Part 5.4 — Cybersecurity Risks of Exposing Stack Traces

Exposing raw Java stack traces to API consumers is a significant security vulnerability:

1. **Package and class names** reveal the internal architecture, making it easier to craft targeted attacks.
2. **Library names and versions** (e.g., `jersey-server-2.41.jar`) allow attackers to look up known CVEs for that exact version.
3. **File system paths** (e.g., `/opt/tomcat/webapps/...`) reveal server directory structure, aiding path traversal attacks.
4. **Business logic flow** is exposed — an attacker can reverse-engineer application behaviour from the call stack.
5. **Database or query fragments** occasionally appear in SQL-related exceptions, providing SQL injection hints.

The Global Exception Mapper logs stack traces server-side (where only administrators can see them) and returns only a generic, non-revealing `500 Internal Server Error` message to the client.

---

### Part 5.5 — Why Use Filters for Cross-Cutting Concerns

Adding `Logger.info()` inside every resource method violates the **DRY principle** and couples business logic with infrastructure concerns. JAX-RS filters implement the **Aspect-Oriented Programming** pattern: cross-cutting concerns (logging, authentication, CORS, compression) are handled in one place and applied universally.

Benefits include: central configuration, consistent log format across all endpoints, ability to toggle or replace the logging implementation without touching any resource method, and cleaner, more readable business logic code. If a new endpoint is added, it automatically inherits logging with zero extra code.

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
