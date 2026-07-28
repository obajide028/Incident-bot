package com.incidentbot.incident_processor.repository;

import com.incidentbot.incident_processor.model.Incident;
import com.incidentbot.incident_processor.model.IncidentStatus;
import com.incidentbot.incident_processor.model.Severity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, UUID> {

    List<Incident> findByServiceName(String serviceName);

    List<Incident> findByStatus(IncidentStatus status);

    List<Incident> findBySeverity(Severity severity);

    List<Incident> findByServiceNameAndStatus(String serviceName, IncidentStatus status);

    List<Incident> findTop10ByOrderByOccurredAtDesc();

    boolean existsByServiceNameAndErrorTypeAndStatusIn(
            String serviceName,
            String errorType,
            List<IncidentStatus> statuses
    );
}
