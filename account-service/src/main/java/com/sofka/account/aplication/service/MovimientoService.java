package com.sofka.account.aplication.service;

import com.sofka.account.api.dto.MovimientoRequest;
import com.sofka.account.api.dto.MovimientoResponse;
import com.sofka.account.api.dto.MovimientoUpdateRequest;
import com.sofka.account.api.mapper.MovimientoMapper;
import com.sofka.account.domain.model.Cuenta;
import com.sofka.account.domain.model.Movimiento;
import com.sofka.account.domain.repository.CuentaRepository;
import com.sofka.account.domain.repository.MovimientoRepository;
import com.sofka.account.shared.SaldoNoDisponibleException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MovimientoService {

    private final MovimientoRepository movRepo;
    private final CuentaRepository cuentaRepo;
    private final MovimientoMapper mapper;

    public MovimientoResponse registrar(String cuentaNumero, MovimientoRequest req) {
        Cuenta c = cuentaRepo.findByNumero(cuentaNumero)
                .orElseThrow(() -> new IllegalArgumentException("Cuenta no existe"));

        BigDecimal nuevoSaldo = c.getSaldoInicial().add(req.getValor());
        if (nuevoSaldo.compareTo(BigDecimal.ZERO) < 0) {
            throw new SaldoNoDisponibleException(cuentaNumero, req.getValor());
        }

        c.setSaldoInicial(nuevoSaldo);

        Movimiento m = mapper.toEntity(req);
        m.setCuenta(c);
        m.setSaldo(nuevoSaldo);

        movRepo.save(m);
        cuentaRepo.save(c);

        return mapper.toResponse(m);
    }

    public MovimientoResponse obtener(Long id) {
        Movimiento m = movRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Movimiento no existe"));
        return mapper.toResponse(m);
    }

    public List<MovimientoResponse> listarPorCuenta(String cuentaNumero) {
        return movRepo.findByCuentaNumeroOrderByFechaAscIdAsc(cuentaNumero)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    public MovimientoResponse actualizar(Long id, MovimientoUpdateRequest req) {
        Movimiento m = movRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Movimiento no existe"));
        // Solo metadatos; no cambiamos 'valor' por consistencia contable
        m.setFecha(req.getFecha());
        m.setTipo(req.getTipo());
        Movimiento saved = movRepo.save(m);
        return mapper.toResponse(saved);
    }

    public void eliminar(Long id) {
        Movimiento m = movRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Movimiento no existe"));

        // Solo se puede borrar el último movimiento de la cuenta
        Movimiento ultimo = movRepo.findTopByCuentaNumeroOrderByFechaDescIdDesc(m.getCuenta().getNumero())
                .orElseThrow(() -> new IllegalStateException("No hay movimientos en la cuenta"));

        if (!ultimo.getId().equals(m.getId())) {
            throw new IllegalStateException("Solo se puede eliminar el último movimiento de la cuenta");
        }

        Cuenta c = m.getCuenta();
        BigDecimal saldoAnterior = ultimo.getSaldo().subtract(m.getValor());
        c.setSaldoInicial(saldoAnterior);
        cuentaRepo.save(c);

        movRepo.deleteById(id);
    }
}
