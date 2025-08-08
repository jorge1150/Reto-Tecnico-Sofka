package com.sofka.account.domain.model;

import java.math.BigDecimal;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Cuenta {

	@Id
	private String numero;

	@Enumerated(EnumType.STRING)
	private TipoCuenta tipo;

	private BigDecimal saldoInicial;

	private Boolean estado = Boolean.TRUE;

	/* relación con cliente por snapshot (id + nombre) para desacoplar */
	private String clienteId;
	private String clienteNombre;
}
