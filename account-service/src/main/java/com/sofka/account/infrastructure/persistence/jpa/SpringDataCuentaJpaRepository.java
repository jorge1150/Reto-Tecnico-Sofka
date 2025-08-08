package com.sofka.account.infrastructure.persistence.jpa;

import com.sofka.account.domain.model.Cuenta;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SpringDataCuentaJpaRepository extends JpaRepository<Cuenta, String> {
    List<Cuenta> findByClienteId(String clienteId);
    boolean existsByNumero(String numero);
}