package com.incidentbot.incident_processor.consumer;

import com.incidentbot.incident_processor.model.IncidentEvent;
import com.incidentbot.incident_processor.service.IncidentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class incidentEventConsumer {

    private final IncidentService incidentService;

    @KafkaListener(
            topics = {"logs.errors", "logs.warnings"},
            groupId = "incident-processor-group"
    )
    public void consume(IncidentEvent event, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.info(
                "Event received - topic[{}] | service: {} | error:{}",
                topic,
                event.serviceName(),
                event.errorType());

        try{
            incidentService.processEvent(event);
        } catch(Exception e){
            log.error("Failed to process event from service [{}]: {}",
                    event.serviceName(),
                    e.getMessage(), e );
        }
    }
}
