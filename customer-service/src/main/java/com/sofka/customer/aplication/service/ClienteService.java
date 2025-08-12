package com.sofka.customer.aplication.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.sofka.customer.api.dto.ClienteRequest;
import com.sofka.customer.api.dto.ClienteResponse;
import com.sofka.customer.api.mapper.ClienteMapper;
import com.sofka.customer.domain.event.ClienteActualizadoEvent;
import com.sofka.customer.domain.model.Cliente;
import com.sofka.customer.domain.repository.IClienteRepository;
import com.sofka.customer.infrastructure.messaging.ClienteEventPublisher;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

/**
 * Capa de aplicación: contiene la lógica de casos de uso. Orquesta repositorios
 * (puertos) y mappers. Es transaccional.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ClienteService {

	private final IClienteRepository repo; // puerto de dominio
	private final ClienteMapper mapper; // DTO <-> Entidad
	private final ClienteEventPublisher eventPublisher;

	public ClienteResponse crear(ClienteRequest request) {
		if (repo.existsByIdentificacion(request.getIdentificacion())) {
			throw new IllegalArgumentException("La identificación ya existe");
		}
		Cliente entity = mapper.toEntity(request);
		Cliente saved = repo.save(entity);
		eventPublisher.publicarClienteActualizado(
				new ClienteActualizadoEvent(saved.getClienteId(), saved.getNombre(), saved.getEstado()));
		return mapper.toResponse(saved);
	}

	public ClienteResponse actualizar(Long id, ClienteRequest req) {
		var c = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));

		c.setNombre(req.getNombre());
		c.setGenero(req.getGenero());
		c.setEdad(req.getEdad());
		c.setDireccion(req.getDireccion());
		c.setTelefono(req.getTelefono());
		Cliente updated = repo.save(c);
		eventPublisher.publicarClienteActualizado(
				new ClienteActualizadoEvent(updated.getClienteId(), updated.getNombre(), updated.getEstado()));
		return mapper.toResponse(updated);
	}

	public ClienteResponse obtenerPorId(Long id) {
		var c = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));
		return mapper.toResponse(c);
	}

	public List<ClienteResponse> listar() {
		return repo.findAll().stream().map(mapper::toResponse).toList();
	}

	public ClienteResponse obtenerPorClienteId(String clienteId) {
		Cliente cliente = repo.findByClienteId(clienteId)
				.orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));
		return mapper.toResponse(cliente);
	}

	public void eliminar(Long id) {
		repo.deleteById(id);
	}

	public ClienteResponse desactivar(Long id) {
		Cliente c = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));
		c.setEstado(false);
		return mapper.toResponse(repo.save(c));
	}
}
