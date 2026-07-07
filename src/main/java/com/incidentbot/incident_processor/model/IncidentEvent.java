package com.incidentbot.incident_processor.model;


import java.time.LocalDateTime;

/**
 *  The message that arrives from Kafka.
 *  Every service publishes this shape when something goes wrong.
 *  Using a Java record - immutable, no boilerplate
 * */
public record IncidentEvent(
        String serviceName,
        String errorType,
        String errorMessage,
        String stackTrace,
        String environment,
        Severity severity,
        LocalDateTime timestamp
) {

    // Compact constructor - fills in defaults if publisher omitted them
    public IncidentEvent {
        if(environment == null || environment.isBlank()) environment = "production";
        if(severity== null) severity = Severity.MEDIUM;
        if(timestamp == null) timestamp = LocalDateTime.now();
    }

}
