package com.sofka.account.domain.model;

/**
 * Tipo de cuenta bancaria. Se persiste con @Enumerated(EnumType.STRING).
 * Mantén los nombres estables para no romper datos.
 */
public enum TipoCuenta {

    AHORROS("Ahorros"),
    CORRIENTE("Corriente");

    private final String descripcion;

    TipoCuenta(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}