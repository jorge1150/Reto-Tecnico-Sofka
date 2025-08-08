package com.sofka.customer.infrastructure.persistence.jpa;

import com.sofka.customer.domain.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
/**
 * Repositorio nativo Spring Data JPA (solo en infraestructura).
 * No lo usamos directamente en aplicación; lo envolvemos en el adaptador.
 */
public interface SpringDataClienteJpaRepository extends JpaRepository<Cliente, Long> {
    Optional<Cliente> findByClienteId(String clienteId);
    boolean existsByIdentificacion(String identificacion);
}