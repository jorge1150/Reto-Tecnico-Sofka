package com.sofka.customer.api.mapper;

import org.springframework.stereotype.Component;

import com.sofka.customer.api.dto.ClienteRequest;
import com.sofka.customer.api.dto.ClienteResponse;
import com.sofka.customer.domain.model.Cliente;

@Component
public class ClienteMapper {

	public Cliente toEntity(ClienteRequest req) {
		Cliente c = new Cliente();
		c.setClienteId(req.getClienteId());
		c.setNombre(req.getNombre());
		c.setGenero(req.getGenero());
		c.setEdad(req.getEdad());
		c.setIdentificacion(req.getIdentificacion());
		c.setDireccion(req.getDireccion());
		c.setTelefono(req.getTelefono());
		c.setPassword(req.getPassword());
		c.setEstado(true);
		return c;
	}

	public ClienteResponse toResponse(Cliente c) {
		ClienteResponse r = new ClienteResponse();
		r.setId(c.getId());
		r.setClienteId(c.getClienteId());
		r.setNombre(c.getNombre());
		r.setGenero(c.getGenero());
		r.setEdad(c.getEdad());
		r.setIdentificacion(c.getIdentificacion());
		r.setDireccion(c.getDireccion());
		r.setTelefono(c.getTelefono());
		r.setEstado(c.getEstado());
		return r;
	}
}