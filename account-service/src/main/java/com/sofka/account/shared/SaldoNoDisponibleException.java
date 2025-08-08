package com.sofka.account.shared;

import java.math.BigDecimal;

public class SaldoNoDisponibleException extends RuntimeException {

    private final String cuentaNumero;
    private final BigDecimal intentoValor;

    public SaldoNoDisponibleException(String cuentaNumero, BigDecimal intentoValor) {
        super("Saldo no disponible para la cuenta " + cuentaNumero + " al intentar aplicar " + intentoValor);
        this.cuentaNumero = cuentaNumero;
        this.intentoValor = intentoValor;
    }

    public String getCuentaNumero() {
        return cuentaNumero;
    }

    public BigDecimal getIntentoValor() {
        return intentoValor;
    }
}