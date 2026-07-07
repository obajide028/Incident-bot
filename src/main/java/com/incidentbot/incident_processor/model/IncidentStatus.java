package com.incidentbot.incident_processor.model;

public enum IncidentStatus {
    OPEN,  // Just received, not yet diagnosed
    DIAGNOSED,  // AI has analysed it
    RESOLVED  // engineer has marked it resolved
}
