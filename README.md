# AI Incident Response Bot

An event-driven microservice that automatically detects, diagnoses, and alerts on production errors using AI. When a service publishes an error event to Kafka, this bot picks it up, sends it to Claude (Anthropic) for root cause analysis, persists the incident to PostgreSQL, and fires a rich Slack alert — all within seconds, with no human involvement.

---

## Architecture

```
 payment-service  ──┐
 auth-service     ──┼──► Kafka (logs.errors / logs.warnings)
 order-service    ──┘              │
                                   ▼
                        IncidentEventConsumer
                                   │
                          ┌────────┴─────────┐
                          ▼                  ▼
                  AiDiagnosticService   SlackNotificationService
                  (Claude via           (Block Kit alert to
                   Spring AI)            #incidents channel)
                          │
                          ▼
                    PostgreSQL
                  (incidents table)
                          │
                          ▼
                   REST API (/api/incidents)
```

Each upstream service catches unhandled exceptions and publishes a structured JSON event to Kafka. The `incident-processor` consumes those events, runs AI diagnosis, persists the result, and sends a Slack alert — without any direct coupling between services.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 23 |
| Framework | Spring Boot 4.1.0 |
| Message Broker | Apache Kafka (KRaft mode) |
| AI Provider | Anthropic Claude (via Spring AI 2.0.0) |
| Database | PostgreSQL 16 |
| ORM | Spring Data JPA + Hibernate 7 |
| Migrations | Flyway |
| Alerts | Slack Incoming Webhooks (Block Kit) |
| Infrastructure | Docker + Docker Compose |

---

## Features

- **Real-time error consumption** — Kafka listener picks up error events the moment they're published
- **AI-powered diagnosis** — Claude analyses each incident and returns root cause, business impact, and a concrete fix in seconds
- **Pluggable AI providers** — switch between Anthropic, OpenAI, or Groq by changing a single config value (`app.ai.provider`)
- **Persistent incident store** — every incident is saved to PostgreSQL with UUID, severity, environment, timestamps, and full AI diagnosis
- **Slack alerts** — rich Block Kit messages sent to your channel on every new incident
- **REST API** — query incidents by service, status, or recency; mark incidents as resolved
- **Resilient consumer** — `ErrorHandlingDeserializer` wraps the Kafka consumer so malformed messages are logged and skipped rather than crashing the listener

---

## Prerequisites

- Java 23
- Docker + Docker Compose
- An [Anthropic API key](https://console.anthropic.com/keys)
- A [Slack Incoming Webhook URL](https://api.slack.com/apps)

---

## Running Locally

**1. Clone the repo**
```bash
git clone https://github.com/obajide028/Incident-bot.git
cd Incident-bot
```

**2. Create your config file**
```bash
cp src/main/resources/application.example.yml src/main/resources/application.yml
```

Fill in your values:
```yaml
spring:
  ai:
    anthropic:
      api-key: YOUR_ANTHROPIC_API_KEY

app:
  ai:
    provider: claude   # or: openai | groq
  slack:
    webhook-url: YOUR_SLACK_WEBHOOK_URL
```

**3. Start infrastructure**
```bash
docker-compose up -d
```

This starts PostgreSQL (port 5433), Kafka (port 9092), and Kafka UI (port 8090).

**4. Run the application**
```bash
./mvnw spring-boot:run
```

Flyway runs the database migration automatically on startup.

---

## Simulating an Incident

### Via REST (no Kafka needed)
```bash
POST http://localhost:8080/api/incidents/simulate

{
  "serviceName": "payment-service",
  "errorType": "NullPointerException",
  "errorMessage": "Cannot invoke method getAmount() on null object reference",
  "stackTrace": "java.lang.NullPointerException\n\tat com.payments.PaymentService.process(PaymentService.java:87)",
  "environment": "production",
  "severity": "HIGH",
  "timestamp": "2026-07-27T10:00:00"
}
```

### Via Kafka
```bash
docker exec -it incident-kafka /opt/kafka/bin/kafka-console-producer.sh \
  --bootstrap-server localhost:9092 --topic logs.errors
```
Then paste a JSON event and press Enter.

---

## REST API

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/incidents` | All incidents |
| GET | `/api/incidents/recent` | Last 10 incidents |
| GET | `/api/incidents/service/{name}` | Incidents by service name |
| GET | `/api/incidents/open` | All open incidents |
| PATCH | `/api/incidents/{id}/resolve` | Mark incident as resolved |
| POST | `/api/incidents/simulate` | Simulate an incident (bypasses Kafka) |

---

## Integrating an Upstream Service

Any service can feed incidents into this bot by publishing to the `logs.errors` Kafka topic:

```java
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleAll(Exception ex) {
        Map<String, Object> event = Map.of(
            "serviceName",  "payment-service",
            "errorType",    ex.getClass().getSimpleName(),
            "errorMessage", ex.getMessage(),
            "stackTrace",   Arrays.toString(ex.getStackTrace()),
            "environment",  "production",
            "severity",     "HIGH",
            "timestamp",    LocalDateTime.now().toString()
        );

        kafkaTemplate.send("logs.errors", event);

        return ResponseEntity.status(500).body("Something went wrong");
    }
}
```

The `incident-processor` requires no changes. It consumes from the topic regardless of which service published.

---

## Switching AI Providers

Change `app.ai.provider` in `application.yml` and add the corresponding dependency and API key:

| Value | Provider | Dependency |
|---|---|---|
| `claude` | Anthropic Claude | `spring-ai-starter-model-anthropic` |
| `openai` | OpenAI GPT | `spring-ai-starter-model-openai` |
| `groq` | Groq (OpenAI-compatible) | `spring-ai-starter-model-openai` + custom base URL |

---

## Project Structure

```
src/main/java/com/incidentbot/incident_processor/
├── ai/
│   └── AiDiagnosticService.java       # Sends incident to Claude, returns diagnosis
├── config/
│   ├── AiConfig.java                  # Pluggable AI provider selection
│   └── KafkaConfig.java               # Topic declarations
├── consumer/
│   └── IncidentEventConsumer.java     # Kafka listener
├── controller/
│   └── IncidentController.java        # REST API
├── model/
│   ├── Incident.java                  # JPA entity
│   ├── IncidentEvent.java             # Kafka message record
│   ├── IncidentStatus.java            # OPEN | DIAGNOSED | RESOLVED
│   └── Severity.java                  # LOW | MEDIUM | HIGH | CRITICAL
├── notification/
│   └── SlackNotificationService.java  # Block Kit Slack alerts
├── repository/
│   └── IncidentRepository.java        # Spring Data JPA
└── service/
    └── IncidentService.java           # Core pipeline orchestration
```
