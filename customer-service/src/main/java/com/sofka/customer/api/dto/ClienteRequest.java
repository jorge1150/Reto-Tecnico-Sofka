package com.sofka.customer.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ClienteRequest {
	@NotBlank
	private String clienteId;
	@NotBlank
	private String nombre;
	@NotBlank
	private String genero;
	@NotNull
	@Min(0)
	private Integer edad;
	@NotBlank
	private String identificacion;
	private String direccion;
	private String telefono;
	@NotBlank
	private String password;
}