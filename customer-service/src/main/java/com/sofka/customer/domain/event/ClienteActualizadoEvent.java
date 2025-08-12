package com.sofka.customer.domain.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Evento de integración para propagar cambios de cliente a otros Bounded Contexts. */
@Data @NoArgsConstructor @AllArgsConstructor
public class ClienteActualizadoEvent {
    private String clienteId;
    private String nombre;
    private Boolean estado;
}
