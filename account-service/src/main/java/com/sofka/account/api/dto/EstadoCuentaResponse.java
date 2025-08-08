package com.sofka.account.api.dto;

import com.sofka.account.domain.model.TipoCuenta;
import com.sofka.account.domain.model.TipoMovimiento;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class EstadoCuentaResponse {
    private LocalDate fecha;
    private String cuentaNumero;
    private TipoMovimiento tipo;
    private BigDecimal valor;
    private BigDecimal saldo;      // saldo resultante tras el movimiento
    private String clienteId;
    private String clienteNombre; 
    private TipoCuenta tipoCuenta;
}
