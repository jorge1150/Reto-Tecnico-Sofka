package com.sofka.customer.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Cliente extends Persona {

	@Column(unique = true, nullable = false)
	private String clienteId;

	private String password;

	private Boolean estado = Boolean.TRUE;

	public void desactivar() {
		this.estado = false;
	}
}
