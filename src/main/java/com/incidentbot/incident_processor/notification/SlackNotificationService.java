package com.incidentbot.incident_processor.notification;

import com.incidentbot.incident_processor.model.Incident;
import com.incidentbot.incident_processor.model.Severity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class SlackNotificationService {

    @Value("${app.slack.webhook-url:}")
    private String webhookUrl;

    private final ObjectMapper objectMapper;
    private final RestClient restClient = RestClient.create();

    public void sendAlert(Incident incident) {
        if (webhookUrl == null || webhookUrl.isBlank()) {
            log.info("Slack webhook not configured — skipping alert for incident: {}", incident.getId());
            return;
        }

        try {
            Map<String, Object> payload = buildPayload(incident);
            String json = objectMapper.writeValueAsString(payload);

            restClient.post()
                    .uri(webhookUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(json)
                    .retrieve()
                    .toBodilessEntity();

            log.info("Slack alert sent for incident: {}", incident.getId());

        } catch (Exception e) {
            log.error("Failed to send Slack alert for incident [{}]: {}", incident.getId(), e.getMessage());
        }
    }

    private Map<String, Object> buildPayload(Incident incident) {
        return Map.of("blocks", List.of(
                headerBlock(severityEmoji(incident.getSeverity()) + " Incident — " + incident.getServiceName()),
                fieldsBlock(incident),
                textBlock("*Error:*\n" + incident.getErrorMessage()),
                textBlock("*AI Diagnosis:*\n" + incident.getAiDiagnosis()),
                contextBlock("ID: `" + incident.getId() + "` | Occurred: " + incident.getOccurredAt())
        ));
    }

    // ── Block builders ────────────────────────────────────────────────────────

    private Map<String, Object> headerBlock(String text) {
        return Map.of(
                "type", "header",
                "text", Map.of("type", "plain_text", "text", text)
        );
    }

    private Map<String, Object> fieldsBlock(Incident incident) {
        return Map.of(
                "type", "section",
                "fields", List.of(
                        mrkdwn("*Service:*\n" + incident.getServiceName()),
                        mrkdwn("*Severity:*\n" + incident.getSeverity()),
                        mrkdwn("*Environment:*\n" + incident.getEnvironment()),
                        mrkdwn("*Status:*\n" + incident.getStatus())
                )
        );
    }

    private Map<String, Object> textBlock(String text) {
        return Map.of(
                "type", "section",
                "text", mrkdwn(text)
        );
    }

    private Map<String, Object> contextBlock(String text) {
        return Map.of(
                "type", "context",
                "elements", List.of(mrkdwn(text))
        );
    }

    private Map<String, String> mrkdwn(String text) {
        return Map.of("type", "mrkdwn", "text", text);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String severityEmoji(Severity severity) {
        return switch (severity) {
            case CRITICAL -> ":red_circle:";
            case HIGH     -> ":orange_circle:";
            case MEDIUM   -> ":yellow_circle:";
            case LOW      -> ":white_circle:";
        };
    }
}
