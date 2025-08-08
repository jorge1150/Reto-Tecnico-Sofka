package com.sofka.account.domain.repository;

import java.util.List;
import java.util.Optional;

import com.sofka.account.domain.model.Cuenta;

public interface CuentaRepository {
    Cuenta save(Cuenta c);

    Optional<Cuenta> findByNumero(String numero);

    List<Cuenta> findByClienteId(String clienteId);

    boolean existsByNumero(String numero);

    void deleteByNumero(String numero);
}