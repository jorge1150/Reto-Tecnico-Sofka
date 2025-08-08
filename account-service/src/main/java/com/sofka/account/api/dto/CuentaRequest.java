package com.sofka.account.api.dto;

import com.sofka.account.domain.model.TipoCuenta;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CuentaRequest {
    @NotBlank private String numero;
    @NotNull  private TipoCuenta tipo;
    @NotNull  private BigDecimal saldoInicial;
    @NotBlank private String clienteId;
    @NotBlank private String clienteNombre;
}