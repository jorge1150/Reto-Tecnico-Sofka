package com.sofka.account.infrastructure.persistence;

import com.sofka.account.domain.model.Movimiento;
import com.sofka.account.domain.repository.MovimientoRepository;
import com.sofka.account.infrastructure.persistence.jpa.SpringDataMovimientoJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MovimientoRepositoryAdapter implements MovimientoRepository {

    private final SpringDataMovimientoJpaRepository jpa;

    @Override
    public Movimiento save(Movimiento m) {
        return jpa.save(m);
    }

    @Override
    public List<Movimiento> findByClienteIdAndFechaBetween(String clienteId, LocalDate desde, LocalDate hasta) {
        return jpa.findByCuenta_ClienteIdAndFechaBetween(clienteId, desde, hasta);
    }

    @Override
    public Optional<Movimiento> findById(Long id) {
        return jpa.findById(id);
    }

    @Override
    public void deleteById(Long id) {
        jpa.deleteById(id);
    }

    @Override
    public List<Movimiento> findByCuentaNumeroOrderByFechaAscIdAsc(String numero) {
        return jpa.findByCuenta_NumeroOrderByFechaAscIdAsc(numero);
    }

    @Override
    public Optional<Movimiento> findTopByCuentaNumeroOrderByFechaDescIdDesc(String numero) {
        return jpa.findTopByCuenta_NumeroOrderByFechaDescIdDesc(numero);
    }

    @Override
    public boolean existsByCuentaNumero(String numero) {
        return jpa.existsByCuenta_Numero(numero);
    }
}