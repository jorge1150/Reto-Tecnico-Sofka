package com.sofka.customer.infrastructure.messaging;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/** Config mínima: crea el tópico si no existe. */
@Configuration
public class KafkaConfig {

    public static final String TOPIC_CLIENTE = "customer.events";

    @Bean
    NewTopic customerEventsTopic() {
        // 3 particiones para throughput; en single-broker replicas=1
        return TopicBuilder.name(TOPIC_CLIENTE)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
