package com.sofka.customer.api.dto;

import lombok.Data;

@Data
public class ClienteResponse {
	private Long id;
	private String clienteId;
	private String nombre;
	private String genero;
	private Integer edad;
	private String identificacion;
	private String direccion;
	private String telefono;
	private Boolean estado;
}