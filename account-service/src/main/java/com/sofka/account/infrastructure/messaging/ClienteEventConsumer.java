package com.sofka.account.infrastructure.messaging;

import com.sofka.account.domain.event.ClienteActualizadoEvent;
import com.sofka.account.domain.model.Cuenta;
import com.sofka.account.domain.repository.CuentaRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Consume eventos de cliente y actualiza el snapshot (clienteNombre) en las cuentas.
 * Idempotente: si el nombre no cambia, igual el save no rompe nada.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ClienteEventConsumer {

    private final CuentaRepository cuentaRepo;

    @KafkaListener(
            topics = KafkaConfig.TOPIC_CLIENTE,
            groupId = "account-service",
            containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void onClienteActualizado(ClienteActualizadoEvent evt) {
        log.info("Recibido ClienteActualizadoEvent: clienteId={} nombre={} estado={}",
                evt.getClienteId(), evt.getNombre(), evt.getEstado());

        List<Cuenta> cuentas = cuentaRepo.findByClienteId(evt.getClienteId());
        if (cuentas.isEmpty()) {
            log.info("No hay cuentas para clienteId={}", evt.getClienteId());
            return;
        }

        for (Cuenta c : cuentas) {
            c.setClienteNombre(evt.getNombre());
            cuentaRepo.save(c);
        }
        log.info("Actualizadas {} cuentas para clienteId={}", cuentas.size(), evt.getClienteId());
    }
}
