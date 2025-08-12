package com.sofka.customer.infrastructure.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.sofka.customer.domain.event.ClienteActualizadoEvent;

/** Publica eventos de Cliente a Kafka (key = clienteId). */
@Component
@RequiredArgsConstructor
@Slf4j
public class ClienteEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate; // JsonSerializer

    public void publicarClienteActualizado(ClienteActualizadoEvent evt) {
        kafkaTemplate.send(KafkaConfig.TOPIC_CLIENTE, evt.getClienteId(), evt)
            .whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Fallo publicando evento clienteId={} : {}", evt.getClienteId(), ex.getMessage(), ex);
                } else {
                    log.info("Evento publicado: topic={} key={} offset={}",
                            result.getRecordMetadata().topic(),
                            evt.getClienteId(),
                            result.getRecordMetadata().offset());
                }
            });
    }
}

