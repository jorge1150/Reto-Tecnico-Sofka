package com.sofka.account.domain.repository;

import com.sofka.account.domain.model.Movimiento;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MovimientoRepository {

    Movimiento save(Movimiento m);

    List<Movimiento> findByClienteIdAndFechaBetween(String clienteId, LocalDate desde, LocalDate hasta);

    Optional<Movimiento> findById(Long id);

    void deleteById(Long id);

    List<Movimiento> findByCuentaNumeroOrderByFechaAscIdAsc(String numero);

    Optional<Movimiento> findTopByCuentaNumeroOrderByFechaDescIdDesc(String numero);

    boolean existsByCuentaNumero(String numero);
}