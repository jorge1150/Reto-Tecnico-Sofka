package com.sofka.customer.domain.repository;

import java.util.List;
import java.util.Optional;

import com.sofka.customer.domain.model.Cliente;

/**
 * Puerto del dominio. No depende de Spring.
 * Define lo que la capa de aplicación necesita de la persistencia.
 */

public interface IClienteRepository {

	Cliente save(Cliente cliente);

	Optional<Cliente> findById(Long id);

	Optional<Cliente> findByClienteId(String clienteId);

	List<Cliente> findAll();

	void deleteById(Long id);

	boolean existsByIdentificacion(String identificacion);
}