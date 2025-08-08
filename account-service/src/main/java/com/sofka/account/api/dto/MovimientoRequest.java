package com.sofka.account.api.dto;

import com.sofka.account.domain.model.TipoMovimiento;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class MovimientoRequest {
    @NotNull private LocalDate fecha;
    @NotNull private TipoMovimiento tipo;
    @NotNull private BigDecimal valor; // positivo=depósito, negativo=retiro
}