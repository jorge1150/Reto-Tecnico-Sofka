package com.sofka.account.infrastructure.persistence;

import com.sofka.account.domain.model.Cuenta;
import com.sofka.account.domain.repository.CuentaRepository;
import com.sofka.account.infrastructure.persistence.jpa.SpringDataCuentaJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CuentaRepositoryAdapter implements CuentaRepository {

    private final SpringDataCuentaJpaRepository jpa;

    @Override
    public Cuenta save(Cuenta c) { return jpa.save(c); }

    @Override
    public Optional<Cuenta> findByNumero(String numero) { return jpa.findById(numero); }

    @Override
    public List<Cuenta> findByClienteId(String clienteId) { return jpa.findByClienteId(clienteId); }

    @Override
    public boolean existsByNumero(String numero) { return jpa.existsByNumero(numero); }

    @Override
    public void deleteByNumero(String numero) {
        jpa.deleteById(numero);
    }
}