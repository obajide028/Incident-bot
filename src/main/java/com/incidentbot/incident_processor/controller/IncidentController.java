package com.incidentbot.incident_processor.controller;

import com.incidentbot.incident_processor.model.Incident;
import com.incidentbot.incident_processor.model.IncidentEvent;
import com.incidentbot.incident_processor.service.IncidentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/incidents")
@RequiredArgsConstructor
@Slf4j
public class IncidentController {

    private final IncidentService incidentService;

    @GetMapping
    public ResponseEntity<List<Incident>> getAllIncidents() {
        return ResponseEntity.ok(incidentService.getAllIncidents());
    }

    @GetMapping("/recent")
    public ResponseEntity<List<Incident>> getRecentIncidents() {
        return ResponseEntity.ok(incidentService.getRecentIncidents());
    }

    @GetMapping("/service/{serviceName}")
    public ResponseEntity<List<Incident>>getByService(@PathVariable String serviceName)
    {
        return ResponseEntity.ok(incidentService.getIncidentsByService(serviceName));
    }

    @GetMapping("/open")
    public ResponseEntity<List<Incident>> getOpenIncidents() {
        return ResponseEntity.ok(incidentService.getOpenIncidents());
    }

    @PatchMapping("/{id}/resolve")
    public ResponseEntity<Incident>resolveIncident(@PathVariable UUID id) {
        return ResponseEntity.ok(incidentService.resolveIncident(id));
    }

    @PostMapping("/simulate")
    public ResponseEntity<String> simulate(@RequestBody IncidentEvent event) {
        log.info("simulating incident for service:  {}", event.serviceName());

        incidentService.processEvent(event);
        return  ResponseEntity.ok("Incident simulated successfully for service:  " + event.serviceName());
    }

}
