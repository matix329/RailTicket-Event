# RailTicket-Event

A comprehensive Spring Boot service for managing railway stations, routes, and ticket purchases with advanced path-finding algorithms, train categories, and seat management. Features Dijkstra's algorithm for optimal route planning with multiple criteria (fastest, cheapest, best value), integrated seat reservation system, and real-time analytics powered by Kafka + Flink streaming.

---

## Table of Contents
- Overview
- Architecture
- Tech Stack
- Quick Start
  - Run locally (Maven + H2)
  - Run with Docker Compose (PostgreSQL + Kafka + Flink)
- Configuration
- API Reference
  - Stations
  - Routes
  - Tickets
  - Transactions
  - Analytics
  - Maintenance
  - Health
- Real-time Analytics (Kafka + Flink)
- Monitoring & Observability
- Data Model
- Project Structure
- Development Notes

---

## Overview
This service exposes REST APIs to:
- Create and list stations
- Create and list routes between stations with train categories (IC, RE, RB)
- Find optimal paths between stations using Dijkstra's algorithm with multiple criteria:
  - **Fastest path** (shortest travel time)
  - **Cheapest path** (lowest price)
  - **Best value path** (best price/time ratio)
- Manage seat capacity and availability for each route
- Reserve and release seats when buying/canceling tickets
- Calculate ticket prices with discounts and record transactions
- Validate ticket existence
- Manage transaction history and user purchases
- **Real-time analytics**: Every ticket purchase and transaction generates Kafka events for live analytics
- **Advanced analytics**: Query aggregated metrics from Flink streaming jobs (route analytics, top routes by sales/revenue)
- **Route maintenance**: Reset seats and adjust capacity for operational management

Use cases include bootstrapping a route graph, finding optimal routes between stations with different priorities, managing train capacity, selling tickets with seat reservations, tracking purchase history through transactions, and analyzing route performance with real-time metrics.

## Architecture
- Monorepo with `backend` and `flink-jobs` modules
- Spring Boot 3.x (Java 21), REST controllers, JPA
- H2 for local development profile, PostgreSQL for dev/docker
- Advanced service layer with DTO mapping, transaction management, and graph algorithms
- In-memory Dijkstra's algorithm with dynamic weight functions for multiple path-finding criteria
- Integrated seat management system with capacity tracking and reservation logic
- Train category system (IC, RE, RB) for route classification
- **Real-time streaming**: Kafka event producers + Apache Flink for live analytics

## Tech Stack
- Java 21, Spring Boot 3.2
- Spring Web, Spring Data JPA, Actuator, Transaction Management
- Spring Kafka for event streaming
- H2 (local), PostgreSQL 15 (docker/dev)
- Apache Kafka 7.4.0 for event streaming
- Apache Flink 1.17.1 for real-time analytics
- Prometheus + Grafana for monitoring and metrics
- Alertmanager for alert management
- Maven, Docker, Docker Compose

---

## Quick Start

### Run locally (Maven + H2)
This uses the default `application.yml` or `application-h2.yml` profiles if configured.

```bash
./mvnw spring-boot:run -pl backend -Dspring-boot.run.profiles=h2

mvn -q -pl backend spring-boot:run -Dspring-boot.run.profiles=h2
```

Backend will start on `http://localhost:8085` by default (port may vary per profile).

### Run with Docker Compose (PostgreSQL + Kafka + Flink)
```bash
docker compose up --build -d

sleep 30

docker exec -it railgraph-flink-jobmanager flink run /opt/flink/usrlib/flink-jobs.jar

# services
# - backend: http://localhost:8085
# - postgres: localhost:55432 (mapped to 5432 in container)
# - pgAdmin: http://localhost:8082
# - Kafka: localhost:29092
# - Kafka UI: http://localhost:8083
# - Flink Dashboard: http://localhost:8081
```

Default compose credentials:
- PostgreSQL: DB `railgraph`, user `railgraph`, password `railgraph123`
- pgAdmin: email `admin@railgraph.com`, password `admin123`

---

## Configuration

### Spring Profiles
- `h2`: local development with in-memory H2
- `dev`: containerized/dev environment with PostgreSQL (enabled in compose)

### Environment variables (compose backend)
- `SPRING_PROFILES_ACTIVE=dev`
- `SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/railgraph`
- `SPRING_DATASOURCE_USERNAME=railgraph`
- `SPRING_DATASOURCE_PASSWORD=railgraph123`
- `KAFKA_BOOTSTRAP_SERVERS=kafka:9092`

### Ports
- Backend: `8085` (host) -> `8080` (container)
- PostgreSQL: `55432` (host) -> `5432` (container)
- pgAdmin: `8082` (host) -> `80` (container)
- Kafka: `29092` (host) -> `29092` (container)
- Kafka UI: `8083` (host) -> `8080` (container)
- Flink Dashboard: `8081` (host) -> `8081` (container)

---

## API Reference
Base URL depends on how you run the service:
- Local H2 (mvn): `http://localhost:8085`
- Docker compose: `http://localhost:8085`

Replace `<BASE_URL>` with your chosen base URL.

### Stations
- Create station
  - POST `<BASE_URL>/routes/stations`
  - Body:
```json
{
  "name": "Köln Hauptbahnhof",
  "city": "Köln"
}
```
  - Response 200:
```json
{
  "id": 1,
  "name": "Köln Hauptbahnhof",
  "city": "Köln"
}
```
- List stations
  - GET `<BASE_URL>/routes/stations`
  - Response 200: `StationOutputDTO[]`

### Routes
- Create route
  - POST `<BASE_URL>/routes`
  - Body:
```json
{
  "stationFromId": 1,
  "stationToId": 2,
  "travelTimeMinutes": 25,
  "price": 12.50,
  "trainCategory": "RE",
  "trainNumber": "RE 100",
  "capacity": 100
}
```
  - Response 200:
```json
{
  "id": 10,
  "stationFrom": { "id": 1, "name": "Köln Hauptbahnhof", "city": "Köln" },
  "stationTo":   { "id": 2, "name": "Düsseldorf Hauptbahnhof", "city": "Düsseldorf" },
  "travelTimeMinutes": 25,
  "price": 12.50,
  "trainCategory": "RE",
  "trainNumber": "RE 100",
  "capacity": 100,
  "availableSeats": 100
}
```
- List routes
  - GET `<BASE_URL>/routes`
  - Response 200: `RouteOutputDTO[]`

#### Path Finding (Dijkstra's Algorithm)
- Find shortest path (by time) - stations only
  - GET `<BASE_URL>/routes/path?fromId={id}&toId={id}`
  - Query Parameters:
    - `fromId`: Source station ID
    - `toId`: Destination station ID
  - Response 200: `StationOutputDTO[]` (ordered list of stations in shortest path)
  - Response 500: Error if station doesn't exist

- Find shortest path with train details
  - GET `<BASE_URL>/routes/path/details?fromId={id}&toId={id}`
  - Query Parameters:
    - `fromId`: Source station ID
    - `toId`: Destination station ID
  - Response 200: `RouteOutputDTO[]` (ordered list of route segments with train information)
  - Response 500: Error if station doesn't exist
  - Example: `GET /routes/path/details?fromId=1&toId=2`
  - Response:
```json
[
  {
    "id": 1,
    "stationFrom": {"id": 1, "name": "Köln Hauptbahnhof", "city": "Köln"},
    "stationTo": {"id": 2, "name": "Düsseldorf Hauptbahnhof", "city": "Düsseldorf"},
    "travelTimeMinutes": 25,
    "price": 12.50,
    "trainCategory": "RE",
    "trainNumber": "RE 100",
    "capacity": 100,
    "availableSeats": 100
  }
]
```

- Find fastest path - with train details
  - GET `<BASE_URL>/routes/path/fastest?fromId={id}&toId={id}`
  - Query Parameters:
    - `fromId`: Source station ID
    - `toId`: Destination station ID
  - Response 200: `RouteOutputDTO[]` (fastest route with train information)
  - Response 500: Error if station doesn't exist
  - Example: `GET /routes/path/fastest?fromId=1&toId=2`
  - Response:
```json
[
  {
    "id": 23,
    "stationFrom": {"id": 1, "name": "Köln Hauptbahnhof", "city": "Köln"},
    "stationTo": {"id": 2, "name": "Düsseldorf Hauptbahnhof", "city": "Düsseldorf"},
    "travelTimeMinutes": 20,
    "price": 15.00,
    "trainCategory": "IC",
    "trainNumber": "IC 2",
    "capacity": 150,
    "availableSeats": 150
  }
]
```

- Find cheapest path - with train details
  - GET `<BASE_URL>/routes/path/cheapest?fromId={id}&toId={id}`
  - Query Parameters:
    - `fromId`: Source station ID
    - `toId`: Destination station ID
  - Response 200: `RouteOutputDTO[]` (cheapest route with train information)
  - Response 500: Error if station doesn't exist
  - Example: `GET /routes/path/cheapest?fromId=1&toId=2`
  - Response:
```json
[
  {
    "id": 21,
    "stationFrom": {"id": 1, "name": "Köln Hauptbahnhof", "city": "Köln"},
    "stationTo": {"id": 2, "name": "Düsseldorf Hauptbahnhof", "city": "Düsseldorf"},
    "travelTimeMinutes": 20,
    "price": 12.50,
    "trainCategory": "IC",
    "trainNumber": "IC 1",
    "capacity": 150,
    "availableSeats": 150
  }
]
```

- Find best value path - with train details
  - GET `<BASE_URL>/routes/path/best-value?fromId={id}&toId={id}`
  - Query Parameters:
    - `fromId`: Source station ID
    - `toId`: Destination station ID
  - Response 200: `RouteOutputDTO[]` (best value route with train information)
  - Response 500: Error if station doesn't exist
  - Example: `GET /routes/path/best-value?fromId=1&toId=2`
  - Response:
```json
[
  {
    "id": 21,
    "stationFrom": {"id": 1, "name": "Köln Hauptbahnhof", "city": "Köln"},
    "stationTo": {"id": 2, "name": "Düsseldorf Hauptbahnhof", "city": "Düsseldorf"},
    "travelTimeMinutes": 20,
    "price": 12.50,
    "trainCategory": "IC",
    "trainNumber": "IC 1",
    "capacity": 150,
    "availableSeats": 150
  }
]
```

**Note:** All path endpoints (`/path/details`, `/path/fastest`, `/path/cheapest`, `/path/best-value`) return full train information (category, number, capacity, available seats) for each route segment. Only `/routes/path` returns stations only.

#### Route Search
- Find all routes between stations
  - GET `<BASE_URL>/routes/search?fromId={id}&toId={id}`
  - Query Parameters:
    - `fromId`: Source station ID
    - `toId`: Destination station ID
  - Response 200: `RouteOutputDTO[]` (all routes between stations, sorted by travel time)
  - Response 500: Error if station doesn't exist
  - Example: `GET /routes/search?fromId=1&toId=2`
  - Response:
```json
[
  {
    "id": 21,
    "stationFrom": {"id": 1, "name": "Köln Hauptbahnhof", "city": "Köln"},
    "stationTo": {"id": 2, "name": "Düsseldorf Hauptbahnhof", "city": "Düsseldorf"},
    "travelTimeMinutes": 20,
    "price": 12.50,
    "trainCategory": "IC",
    "trainNumber": "IC 1",
    "capacity": 150,
    "availableSeats": 150
  },
  {
    "id": 22,
    "stationFrom": {"id": 1, "name": "Köln Hauptbahnhof", "city": "Köln"},
    "stationTo": {"id": 2, "name": "Düsseldorf Hauptbahnhof", "city": "Düsseldorf"},
    "travelTimeMinutes": 20,
    "price": 12.50,
    "trainCategory": "IC",
    "trainNumber": "IC 2",
    "capacity": 150,
    "availableSeats": 150
  },
  {
    "id": 1,
    "stationFrom": {"id": 1, "name": "Köln Hauptbahnhof", "city": "Köln"},
    "stationTo": {"id": 2, "name": "Düsseldorf Hauptbahnhof", "city": "Düsseldorf"},
    "travelTimeMinutes": 25,
    "price": 12.50,
    "trainCategory": "RE",
    "trainNumber": "RE 100",
    "capacity": 100,
    "availableSeats": 100
  }
]
```

- Find available routes between stations
  - GET `<BASE_URL>/routes/search/available?fromId={id}&toId={id}`
  - Query Parameters:
    - `fromId`: Source station ID
    - `toId`: Destination station ID
  - Response 200: `RouteOutputDTO[]` (only routes with available seats, sorted by travel time)
  - Response 500: Error if station doesn't exist
  - Example: `GET /routes/search/available?fromId=1&toId=2`
  - Response:
```json
[
  {
    "id": 23,
    "stationFrom": {"id": 1, "name": "Köln Hauptbahnhof", "city": "Köln"},
    "stationTo": {"id": 2, "name": "Düsseldorf Hauptbahnhof", "city": "Düsseldorf"},
    "travelTimeMinutes": 20,
    "price": 15.00,
    "trainCategory": "IC",
    "trainNumber": "IC 2",
    "capacity": 150,
    "availableSeats": 150
  },
  {
    "id": 1,
    "stationFrom": {"id": 1, "name": "Köln Hauptbahnhof", "city": "Köln"},
    "stationTo": {"id": 2, "name": "Düsseldorf Hauptbahnhof", "city": "Düsseldorf"},
    "travelTimeMinutes": 25,
    "price": 12.50,
    "trainCategory": "RE",
    "trainNumber": "RE 100",
    "capacity": 100,
    "availableSeats": 100
  }
]
```

#### Seat Management
- Get available seats
  - GET `<BASE_URL>/routes/{id}/seats/availability`
  - Response 200: `int` (number of available seats)

- Get total capacity
  - GET `<BASE_URL>/routes/{id}/seats/capacity`
  - Response 200: `int` (total seat capacity)

- Reserve seats
  - POST `<BASE_URL>/routes/{id}/seats/reserve?quantity={number}`
  - Query Parameters:
    - `quantity`: Number of seats to reserve
  - Response 200: Success (seats reserved)
  - Response 400: Not enough seats available

- Release seats
  - POST `<BASE_URL>/routes/{id}/seats/release?quantity={number}`
  - Query Parameters:
    - `quantity`: Number of seats to release
  - Response 200: Success (seats released)

### Tickets
- Buy ticket
  - POST `<BASE_URL>/tickets?userId={userId}`
  - Query Parameters:
    - `userId` (optional): User ID for transaction recording (default: "user123")
  - Body:
```json
{
  "routeId": 10,
  "quantity": 2,
  "discountType": "STUDENT"  
}
```
  - `discountType`: one of `NONE`, `STUDENT`, `SENIOR`, `GROUP`
  - **Note**: Each ticket purchase automatically creates a transaction record
  - Response 200:
```json
{
  "id": 100,
  "route": {
    "id": 10,
    "stationFrom": { "id": 1, "name": "Köln Hauptbahnhof", "city": "Köln" },
    "stationTo":   { "id": 2, "name": "Düsseldorf Hauptbahnhof", "city": "Düsseldorf" },
    "travelTimeMinutes": 25,
    "price": 12.50,
    "trainCategory": "RE",
    "trainNumber": "RE 100",
    "capacity": 100,
    "availableSeats": 98
  },
  "basePrice": 25.00,
  "finalPrice": 12.50,
  "discountType": "STUDENT",
  "quantity": 2
}
```
- List tickets
  - GET `<BASE_URL>/tickets`
  - Response 200: `TicketOutputDTO[]`
- Validate ticket (existence)
  - GET `<BASE_URL>/tickets/{id}/validate`
  - Response 200: `true | false`

### Transactions
- Create transaction
  - POST `<BASE_URL>/transactions`
  - Body:
```json
{
  "ticketId": 100,
  "userId": "user123"
}
```
  - Response 200:
```json
{
  "id": 1,
  "ticket": {
    "id": 100,
    "route": {
      "id": 10,
      "stationFrom": { "id": 1, "name": "Köln Hauptbahnhof", "city": "Köln" },
      "stationTo": { "id": 2, "name": "Düsseldorf Hauptbahnhof", "city": "Düsseldorf" },
      "travelTimeMinutes": 25,
      "price": 12.50,
      "trainCategory": "RE",
      "trainNumber": "RE 100",
      "capacity": 100,
      "availableSeats": 98
    },
    "basePrice": 25.00,
    "finalPrice": 12.50,
    "discountType": "STUDENT",
    "quantity": 2
  },
  "timestamp": "2024-01-15T10:30:00",
  "userId": "user123"
}
```
- List all transactions
  - GET `<BASE_URL>/transactions`
  - Response 200: `TransactionOutputDTO[]`
- Get transaction by ID
  - GET `<BASE_URL>/transactions/{id}`
  - Response 200: `TransactionOutputDTO`
- Get transactions by user
  - GET `<BASE_URL>/transactions/user/{userId}`
  - Response 200: `TransactionOutputDTO[]`

### Analytics

Query aggregated metrics from Flink streaming jobs that process ticket and transaction events in real-time.

- Get route analytics
  - GET `<BASE_URL>/analytics/routes/{routeId}?lastHours={hours}`
  - Query Parameters:
    - `lastHours` (optional): Number of hours to look back (default: 24)
  - Response 200:
```json
[
  {
    "id": 1,
    "routeId": 10,
    "windowStart": "2024-01-15T10:00:00",
    "windowEnd": "2024-01-15T10:05:00",
    "ticketsSold": 15,
    "totalRevenue": 187.50,
    "averagePrice": 12.50,
    "createdAt": "2024-01-15T10:05:01"
  }
]
```

- Get all analytics
  - GET `<BASE_URL>/analytics/routes?lastHours={hours}`
  - Query Parameters:
    - `lastHours` (optional): Number of hours to look back (default: 24)
  - Response 200: `RouteAnalyticsOutputDTO[]` (all routes)

- Get top routes by sales
  - GET `<BASE_URL>/analytics/top-routes/sales?topN={n}&lastHours={hours}`
  - Query Parameters:
    - `topN` (optional): Number of top routes to return (default: 5)
    - `lastHours` (optional): Number of hours to look back (default: 24)
  - Response 200:
```json
[
  {
    "rank": 1,
    "routeId": 10,
    "routeName": "Köln Hauptbahnhof → Düsseldorf Hauptbahnhof",
    "ticketsSold": 150,
    "totalRevenue": 1875.00
  }
]
```

- Get top routes by revenue
  - GET `<BASE_URL>/analytics/top-routes/revenue?topN={n}&lastHours={hours}`
  - Query Parameters:
    - `topN` (optional): Number of top routes to return (default: 5)
    - `lastHours` (optional): Number of hours to look back (default: 24)
  - Response 200: `TopRouteDTO[]`

- Get route summary
  - GET `<BASE_URL>/analytics/routes/{routeId}/summary?lastHours={hours}`
  - Query Parameters:
    - `lastHours` (optional): Number of hours to look back (default: 24)
  - Response 200:
```json
{
  "routeId": 10,
  "routeName": "Köln Hauptbahnhof → Düsseldorf Hauptbahnhof",
  "periodHours": 24,
  "totalTicketsSold": 150,
  "totalRevenue": 1875.0,
  "since": "2024-01-14T10:00:00"
}
```

### Maintenance

Operational endpoints for route capacity management.

- Reset all route seats to full capacity
  - POST `<BASE_URL>/maintenance/reset-seats`
  - Response 200:
```json
{
  "updated_routes": 50,
  "total_routes": 50,
  "total_capacity": 5000,
  "total_available": 5000
}
```

- Increase route capacity
  - POST `<BASE_URL>/maintenance/increase-capacity?multiplier={n}`
  - Query Parameters:
    - `multiplier` (optional): Capacity multiplier between 1 and 100 (default: 10)
  - Response 200:
```json
{
  "updated_routes": 50,
  "multiplier": 10,
  "min_capacity": 1000,
  "max_capacity": 1500,
  "total_capacity": 50000
}
```

- Get capacity statistics
  - GET `<BASE_URL>/maintenance/capacity-stats`
  - Response 200:
```json
{
  "total_routes": 50,
  "total_capacity": 5000,
  "total_available": 4200,
  "total_sold": 800,
  "average_availability_percent": 84.0,
  "routes_below_20_percent": 3
}
```

### Health
- GET `<BASE_URL>/actuator/health`

---

## Real-time Analytics (Kafka + Flink)

The system includes real-time analytics powered by Kafka event streaming and Apache Flink processing.

### Event Streaming Architecture
```
Backend (Spring Boot) → Kafka → Flink → Analytics Dashboard
```

### Event Types

#### TicketEvent
Emitted when a ticket is purchased:
```json
{
  "routeId": 1,
  "quantity": 2,
  "discountType": "STUDENT",
  "userId": "user123",
  "timestamp": "2024-01-01T10:00:00"
}
```

#### TransactionEvent
Emitted when a transaction is created:
```json
{
  "ticketId": 1,
  "userId": "user123",
  "timestamp": "2024-01-01T10:00:00"
}
```

### Flink Analytics Jobs

#### AdvancedTicketAnalyticsJob
Processes ticket events and stores aggregated analytics in PostgreSQL:
- **Input**: `ticket-events` Kafka topic
- **Processing**: Groups by `routeId` and aggregates sales metrics
- **Window**: 5-minute tumbling windows
- **Output**: Stores `RouteAnalytics` records in PostgreSQL with:
  - Tickets sold per route
  - Total revenue
  - Average price
  - Window timestamps

#### TopRoutesAnalyticsJob
Identifies top-performing routes in real-time:
- **Input**: `ticket-events` Kafka topic
- **Processing**: Ranks routes by ticket sales
- **Window**: 10-minute tumbling windows
- **Output**: Live leaderboard of top 5 routes

Example output:
```
========== TOP 5 ROUTES ==========
  1. Route 10: 150 tickets sold
  2. Route 5: 120 tickets sold
  3. Route 3: 95 tickets sold
====================================
```

### Monitoring

- **Kafka UI**: http://localhost:8083 - browse topics, messages, and consumer groups
- **Flink Dashboard**: http://localhost:8081 - manage and monitor Flink jobs
- **Kafka Topics**: `ticket-events`, `transaction-events`
- **Flink Logs**: `docker logs railgraph-flink-taskmanager -f`

---

## Monitoring & Observability

The system includes comprehensive monitoring and alerting using Prometheus, Grafana, and Alertmanager.

### Monitoring Stack

- **Prometheus**: http://localhost:9090 - metrics collection and querying
- **Grafana**: http://localhost:3000 - visualization dashboards (admin/admin)
- **Alertmanager**: http://localhost:9093 - alert management and routing

### Metrics Collection

Prometheus scrapes metrics from:
- **Backend**: Spring Boot Actuator metrics (`/actuator/prometheus`)
- **Kafka**: Kafka Exporter metrics (topics, partitions, consumer lag)
- **Flink JobManager**: Job metrics, checkpoints, restarts
- **Flink TaskManager**: Task metrics, backpressure, records processed

### Grafana Dashboards

Pre-configured dashboards available at http://localhost:3000:

#### RailGraph Overview Dashboard
- Kafka message rates (ticket/transaction events)
- Consumer lag monitoring
- Flink job health (uptime, restarts)
- Backend API metrics (request rates, error rates)
- JVM memory usage

#### RailGraph Scaling Dashboard
- Flink parallelism and task slots
- Kafka partition distribution
- Consumer group lag trends
- Resource utilization metrics

### Alert Rules

The system monitors critical metrics and fires alerts for:

#### Kafka Alerts
- **KafkaConsumerLagHigh**: Consumer lag > 1000 messages
- **KafkaConsumerLagCritical**: Consumer lag > 5000 messages
- **KafkaNoMessagesProduced**: No messages in 10 minutes

#### Flink Alerts
- **FlinkHighBackpressure**: Task backpressure > 500ms/s
- **FlinkCheckpointDurationHigh**: Checkpoint duration > 2 minutes
- **FlinkCheckpointFailed**: Multiple checkpoint failures
- **FlinkNoRecordsProcessed**: No records processed in 10 minutes
- **FlinkFrequentRestarts**: Job restarting frequently

#### Backend Alerts
- **BackendHighErrorRate**: 5xx error rate > 0.1/sec
- **BackendHighMemoryUsage**: JVM heap usage > 85%
- **BackendDown**: Backend API not responding

#### System Alerts
- **ServiceDown**: Any component not responding
- **SlowScrapeTarget**: Prometheus scrape duration > 5s

### Alert Routing

Alerts are routed to different receivers based on severity and component:
- **Critical alerts**: Immediate notification (0s wait)
- **Kafka alerts**: Routed to kafka-team receiver
- **Flink alerts**: Routed to flink-team receiver
- **Backend alerts**: Routed to backend-team receiver

### Accessing Metrics

Query Prometheus metrics directly:
```bash
curl http://localhost:9090/api/v1/query?query=kafka_consumergroup_lag

curl http://localhost:9090/api/v1/query?query=flink_jobmanager_job_uptime
```

View active alerts:
```bash
curl http://localhost:9093/api/v2/alerts
```

---

## cURL Examples
Assuming compose environment (`BASE_URL=http://localhost:8085`).

```bash
# create stations
curl -s -X POST "$BASE_URL/routes/stations" -H 'Content-Type: application/json' \
  -d '{"name":"Köln Hauptbahnhof","city":"Köln"}' | jq .

curl -s -X POST "$BASE_URL/routes/stations" -H 'Content-Type: application/json' \
  -d '{"name":"Düsseldorf Hauptbahnhof","city":"Düsseldorf"}' | jq .

# create a route
curl -s -X POST "$BASE_URL/routes" -H 'Content-Type: application/json' \
  -d '{"stationFromId":1,"stationToId":2,"travelTimeMinutes":25,"price":12.50,"trainCategory":"RE","trainNumber":"RE 100","capacity":100}' | jq .

# buy a ticket with student discount
curl -s -X POST "$BASE_URL/tickets?userId=user123" -H 'Content-Type: application/json' \
  -d '{"routeId":10,"quantity":2,"discountType":"STUDENT"}' | jq .

# list routes
curl -s "$BASE_URL/routes" | jq .

# find shortest path from station 1 to station 2 (stations only)
curl -s "$BASE_URL/routes/path?fromId=1&toId=2" | jq .

# find shortest path with train details from station 1 to station 2
curl -s "$BASE_URL/routes/path/details?fromId=1&toId=2" | jq .

# find fastest path with train details from station 1 to station 2
curl -s "$BASE_URL/routes/path/fastest?fromId=1&toId=2" | jq .

# find cheapest path with train details from station 1 to station 2
curl -s "$BASE_URL/routes/path/cheapest?fromId=1&toId=2" | jq .

# find best value path with train details from station 1 to station 2
curl -s "$BASE_URL/routes/path/best-value?fromId=1&toId=2" | jq .

# find all routes between station 1 and station 2
curl -s "$BASE_URL/routes/search?fromId=1&toId=2" | jq .

# validate ticket id=100
curl -s "$BASE_URL/tickets/100/validate"

# create transaction
curl -s -X POST "$BASE_URL/transactions" -H 'Content-Type: application/json' \
  -d '{"ticketId":100,"userId":"user123"}' | jq .

# list all transactions
curl -s "$BASE_URL/transactions" | jq .

# get transaction by id
curl -s "$BASE_URL/transactions/1" | jq .

# get transactions by user
curl -s "$BASE_URL/transactions/user/user123" | jq .

# get route analytics for route 10 (last 24 hours)
curl -s "$BASE_URL/analytics/routes/10?lastHours=24" | jq .

# get all analytics (last 12 hours)
curl -s "$BASE_URL/analytics/routes?lastHours=12" | jq .

# get top 5 routes by sales
curl -s "$BASE_URL/analytics/top-routes/sales?topN=5&lastHours=24" | jq .

# get top 3 routes by revenue
curl -s "$BASE_URL/analytics/top-routes/revenue?topN=3&lastHours=24" | jq .

# get route summary for route 10
curl -s "$BASE_URL/analytics/routes/10/summary?lastHours=24" | jq .

# reset all route seats to full capacity
curl -s -X POST "$BASE_URL/maintenance/reset-seats" | jq .

# increase capacity by 10x
curl -s -X POST "$BASE_URL/maintenance/increase-capacity?multiplier=10" | jq .

# get capacity statistics
curl -s "$BASE_URL/maintenance/capacity-stats" | jq .
```

---

## Data Model
- `Station` — id, name, city
- `Route` — id, stationFrom, stationTo, travelTimeMinutes, price, trainCategory, trainNumber, capacity, availableSeats
- `Ticket` — id, route, basePrice, finalPrice, discountType, quantity
- `Transaction` — id, ticket, timestamp, userId (links to Ticket)
- `RouteAnalytics` — id, routeId, windowStart, windowEnd, ticketsSold, totalRevenue, averagePrice, createdAt
- `DiscountType` — enum: `NONE`, `STUDENT`, `SENIOR`, `GROUP`
- `TrainCategory` — enum: `IC`, `RE`, `RB`

### Relationships
- `Route` → `Station` (many-to-one for from/to stations)
- `Ticket` → `Route` (many-to-one)
- `Transaction` → `Ticket` (many-to-one)
- `RouteAnalytics` → `Route` (many-to-one, aggregated from Flink)

### API Endpoints Summary
- **Stations**: `/routes/stations` (POST, GET)
- **Routes**: `/routes` (POST, GET)
- **Path Finding**:
  - `/routes/path` (GET) - stations only
  - `/routes/path/details` (GET) - shortest path with train information
  - `/routes/path/fastest` (GET) - fastest path with train information
  - `/routes/path/cheapest` (GET) - cheapest path with train information
  - `/routes/path/best-value` (GET) - best value path with train information
- **Route Search**:
  - `/routes/search` (GET) - all routes between stations
  - `/routes/search/available` (GET) - only routes with available seats
- **Seat Management**: `/routes/{id}/seats/availability` (GET), `/routes/{id}/seats/capacity` (GET), `/routes/{id}/seats/reserve` (POST), `/routes/{id}/seats/release` (POST)
- **Tickets**: `/tickets` (POST, GET), `/tickets/{id}/validate` (GET)
- **Transactions**: `/transactions` (POST, GET), `/transactions/{id}` (GET), `/transactions/user/{userId}` (GET)
- **Analytics**:
  - `/analytics/routes/{routeId}` (GET) - route analytics by ID
  - `/analytics/routes` (GET) - all analytics
  - `/analytics/top-routes/sales` (GET) - top routes by sales
  - `/analytics/top-routes/revenue` (GET) - top routes by revenue
  - `/analytics/routes/{routeId}/summary` (GET) - route summary
- **Maintenance**:
  - `/maintenance/reset-seats` (POST) - reset all seats to full capacity
  - `/maintenance/increase-capacity` (POST) - increase route capacity
  - `/maintenance/capacity-stats` (GET) - capacity statistics

---

## Project Structure
```
RailTicket-Event/
  backend/                    # Spring Boot API
    src/main/java/com/railgraph/
      controller/             # REST controllers
        AnalyticsController.java
        RouteMaintenanceController.java
        StationController.java
        TicketController.java
        TransactionController.java
      service/                # business logic
        AnalyticsService.java
        RouteService.java
        TicketService.java
        TransactionService.java
      dto/                    # input/output DTOs
        output/
          RouteAnalyticsOutputDTO.java
          TopRouteDTO.java
      model/                  # JPA entities and enums
        RouteAnalytics.java
        Station.java
        Route.java
        Ticket.java
        Transaction.java
      repository/             # Spring Data repositories
        RouteAnalyticsRepository.java
      config/                 # data initialization
      event/                  # Kafka event models and producers
        TicketEvent.java
        TicketEventProducer.java
        TransactionEvent.java
        TransactionEventProducer.java
    src/main/resources/
      application.yml
      application-h2.yml
  flink-jobs/                 # Apache Flink streaming jobs
    src/main/java/com/railgraph/flink/
      TicketEvent.java
      AdvancedTicketAnalyticsJob.java
      TopRoutesAnalyticsJob.java
      model/
        RouteAnalytics.java
      aggregator/
        RouteWindowAggregate.java
  monitoring/                 # Monitoring and observability
    prometheus/
      prometheus.yml
      alerts/
        railgraph-alerts.yml
    grafana/
      dashboards/
        railgraph-overview.json
        railgraph-scaling.json
      provisioning/
    alertmanager/
      alertmanager.yml
    flink/
      flink-conf.yaml
  Dockerfile
  docker-compose.yaml
  build-and-run.sh           # Quick start script
  pom.xml (root, dependency management)
```