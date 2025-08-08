package com.sofka.account.domain.model;

/**
 * Tipo de movimiento contable. El servicio suma el valor al saldo:
 * - Si es DEPOSITO, normalmente enviar valor positivo.
 * - Si es RETIRO, normalmente enviar valor negativo.
 * 
 * Nota: el saldo se calcula en la capa de aplicación sumando el valor recibido
 * (positivo/negativo). Estos flags sirven para reglas de negocio y
 * validaciones.
 */
public enum TipoMovimiento {
    DEPOSITO,
    RETIRO;

    public boolean esDeposito() {
        return this == DEPOSITO;
    }

    public boolean esRetiro() {
        return this == RETIRO;
    }
}