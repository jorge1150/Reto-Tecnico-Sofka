package com.sofka.account.api.dto;

import com.sofka.account.domain.model.TipoCuenta;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CuentaResponse {
    private String numero;
    private TipoCuenta tipo;
    private BigDecimal saldoInicial;
    private Boolean estado;
    private String clienteId;
    private String clienteNombre;
}
