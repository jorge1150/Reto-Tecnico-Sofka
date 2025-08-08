package com.sofka.customer.infrastructure.persistence;

import com.sofka.customer.domain.model.Cliente;
import com.sofka.customer.domain.repository.IClienteRepository;
import com.sofka.customer.infrastructure.persistence.jpa.SpringDataClienteJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Adaptador que implementa el puerto del dominio usando Spring Data.
 * Aquí podemos añadir mapeos adicionales, caché, queries específicas, etc.
 */
@Repository
@RequiredArgsConstructor
public class ClienteRepositoryAdapter implements IClienteRepository {

    private final SpringDataClienteJpaRepository jpa;

    @Override
    public Cliente save(Cliente cliente) {
        return jpa.save(cliente);
    }

    @Override
    public Optional<Cliente> findById(Long id) {
        return jpa.findById(id);
    }

    @Override
    public Optional<Cliente> findByClienteId(String clienteId) {
        return jpa.findByClienteId(clienteId);
    }

    @Override
    public List<Cliente> findAll() {
        return jpa.findAll();
    }

    @Override
    public void deleteById(Long id) {
        jpa.deleteById(id);
    }

    @Override
    public boolean existsByIdentificacion(String identificacion) {
        return jpa.existsByIdentificacion(identificacion);
    }
}