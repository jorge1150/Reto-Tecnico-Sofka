package com.sofka.account.domain.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
public class ClienteActualizadoEvent {
    private String clienteId;
    private String nombre;
    private Boolean estado;
}
