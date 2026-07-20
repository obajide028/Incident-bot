package com.incidentbot.incident_processor.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@EnableKafka
public class KafaConfig {

    /**
     *
     *  Declaring Topics here means Kafka creates them automatically on startup
     *  If they don't already exist - no manual setup needed
     * */

    @Bean
    public NewTopic errorsTopic(){
        return TopicBuilder.name("logs.errors")
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic warningsTopic(){
        return TopicBuilder.name("logs.warnings")
                .partitions(1)
                .replicas(1)
                .build();
    }
}
