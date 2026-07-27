package com.incidentbot.incident_processor.service;


import com.incidentbot.incident_processor.ai.AiDiagnosticService;
import com.incidentbot.incident_processor.model.Incident;
import com.incidentbot.incident_processor.model.IncidentEvent;
import com.incidentbot.incident_processor.model.IncidentStatus;
import com.incidentbot.incident_processor.notification.SlackNotificationService;
import com.incidentbot.incident_processor.repository.IncidentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class IncidentService {

    private final IncidentRepository incidentRepository;
    private final AiDiagnosticService aiDiagnosticService;
    private final SlackNotificationService sLackNotificationService;

    /***
     *  Full pipeline: receive event -> AI diagnosis -> save -> notify
     */
    @Transactional
    public void processEvent(IncidentEvent event){
        log.info("Processing incident service: {}", event.serviceName());

        // step 1 - get Ai diagnosis

        String diagnosis = aiDiagnosticService.diagnose(event);

        // Step 2 - build and save the incident
        Incident incident = Incident.builder()
                .serviceName(event.serviceName())
                .errorType(event.errorType())
                .errorMessage(event.errorMessage())
                .stackTrace(event.stackTrace())
                .environment(event.environment())
                .severity(event.severity())
                .status(IncidentStatus.DIAGNOSED)
                .aiDiagnosis(diagnosis)
                .occurredAt(event.timestamp())
                .diagnosedAt(LocalDateTime.now())
                .build();

        Incident saved = incidentRepository.save(incident);
        log.info("Incident saved: {}", saved.getId(), saved.getServiceName());
    }

    public List<Incident> getAllIncidents() {
        return incidentRepository.findAll();
    }

    public List<Incident> getRecentIncidents() {
        return incidentRepository.findTop10ByOrderByOccurredAtDesc();
    }

    public List<Incident> getIncidentsByService(String serviceName) {
        return incidentRepository.findByServiceName(serviceName);
    }

    public List<Incident> getOpenIncidents() {
        return incidentRepository.findByStatus(IncidentStatus.OPEN);
    }

    @Transactional
    public Incident resolveIncident(UUID id) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Incident not found: " + id));

        incident.setStatus(IncidentStatus.RESOLVED);
        return incidentRepository.save(incident);
    }
}
