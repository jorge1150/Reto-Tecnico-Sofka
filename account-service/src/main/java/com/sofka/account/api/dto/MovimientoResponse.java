package com.sofka.account.api.dto;

import com.sofka.account.domain.model.TipoMovimiento;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class MovimientoResponse {
    private Long id;
    private LocalDate fecha;
    private TipoMovimiento tipo;
    private BigDecimal valor;
    private BigDecimal saldo;  // saldo resultante
    private String cuentaNumero;
}