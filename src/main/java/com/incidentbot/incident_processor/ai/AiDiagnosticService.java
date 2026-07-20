package com.incidentbot.incident_processor.ai;


import com.incidentbot.incident_processor.model.IncidentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AiDiagnosticService {

    private final ChatClient chatClient;

    /**
     *  Sends the error context to OpenAI and return a plain-text diagnosis.
     */
     public String diagnose(IncidentEvent event){
         log.info("Sending Incident to Ai for diagnosis - service: {}",
                 event.serviceName());

         String prompt = buildPrompt(event);

         try{
             String diagnosis = chatClient.prompt()
                     .user(prompt)
                     .call()
                     .content();

             log.info("AI diagnosis received for service: {}", event.serviceName());

             return diagnosis;

         } catch (Exception e){
             log.error("AI diagnosis failed for service [{}]: {}", event.serviceName(), e.getMessage());
                  return "AI diagnosis unavailable: " + e.getMessage();
         }
     }

    private String buildPrompt(IncidentEvent event) {
        return """
                You are a senior software engineer specialising in incident response and root cause analysis.
 
                An error has occurred in a production service. Analyse it and respond with:
                1. Root Cause — what most likely caused this error
                2. Impact — what is affected and how severely
                3. Suggested Fix — concrete steps the on-call engineer should take immediately
 
                Keep your response concise and technical. No fluff.
 
                === INCIDENT DETAILS ===
                Service:      %s
                Environment:  %s
                Severity:     %s
                Error Type:   %s
                Error Message: %s
 
                Stack Trace:
                %s
                ========================
                """.formatted(
                event.serviceName(),
                event.environment(),
                event.severity(),
                event.errorType() != null ? event.errorType() : "Unknown",
                event.errorMessage(),
                event.stackTrace() != null ? event.stackTrace() : "No stack trace provided"
        );
    }
}
