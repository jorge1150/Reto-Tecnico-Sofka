package com.sofka.account.infrastructure.persistence.jpa;

import com.sofka.account.domain.model.Movimiento;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SpringDataMovimientoJpaRepository extends JpaRepository<Movimiento, Long> {

    List<Movimiento> findByCuenta_NumeroAndFechaBetween(String numero, LocalDate desde, LocalDate hasta);

    @EntityGraph(attributePaths = "cuenta")
    List<Movimiento> findByCuenta_ClienteIdAndFechaBetween(String clienteId, LocalDate desde, LocalDate hasta);

    List<Movimiento> findByCuenta_NumeroOrderByFechaAscIdAsc(String numero);

    Optional<Movimiento> findTopByCuenta_NumeroOrderByFechaDescIdDesc(String numero);

    boolean existsByCuenta_Numero(String numero);
}
