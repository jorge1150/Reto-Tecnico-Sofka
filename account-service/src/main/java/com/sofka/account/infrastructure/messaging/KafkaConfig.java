package com.sofka.account.infrastructure.messaging;

import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import com.sofka.account.domain.event.ClienteActualizadoEvent;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    public static final String TOPIC_CLIENTE = "customer.events";

    @Bean
    public ConsumerFactory<String, ClienteActualizadoEvent> clienteUpdatedConsumerFactory(
            org.springframework.core.env.Environment env) {
        Map<String, Object> props = new HashMap<>();
        props.put(org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                env.getProperty("spring.kafka.bootstrap-servers", "kafka:9092"));
        props.put(org.apache.kafka.clients.consumer.ConsumerConfig.GROUP_ID_CONFIG, "account-service");
        props.put(org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        JsonDeserializer<ClienteActualizadoEvent> valueDeserializer = new JsonDeserializer<>(
                ClienteActualizadoEvent.class, false);
        valueDeserializer.addTrustedPackages("com.sofka.*");

        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), valueDeserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ClienteActualizadoEvent> kafkaListenerContainerFactory(
            ConsumerFactory<String, ClienteActualizadoEvent> cf) {
        ConcurrentKafkaListenerContainerFactory<String, ClienteActualizadoEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(cf);
        factory.setConcurrency(3); // 3 hilos (match con particiones)
        return factory;
    }
}
