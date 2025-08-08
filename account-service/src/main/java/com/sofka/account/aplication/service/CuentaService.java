package com.sofka.account.aplication.service;

import com.sofka.account.api.dto.CuentaRequest;
import com.sofka.account.api.dto.CuentaResponse;
import com.sofka.account.api.mapper.CuentaMapper;
import com.sofka.account.domain.model.Cuenta;
import com.sofka.account.domain.repository.CuentaRepository;
import com.sofka.account.domain.repository.MovimientoRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CuentaService {

    private final CuentaRepository repo;
    private final MovimientoRepository movRepo;
    private final CuentaMapper mapper;

    public CuentaResponse crear(CuentaRequest req) {
        if (repo.existsByNumero(req.getNumero())) {
            throw new IllegalArgumentException("La cuenta ya existe");
        }
        Cuenta c = mapper.toEntity(req);
        return mapper.toResponse(repo.save(c));
    }

    public List<CuentaResponse> listarPorCliente(String clienteId) {
        return repo.findByClienteId(clienteId).stream().map(mapper::toResponse).toList();
    }

    public CuentaResponse obtener(String numero) {
        var c = repo.findByNumero(numero).orElseThrow(() -> new IllegalArgumentException("Cuenta no existe"));
        return mapper.toResponse(c);
    }

    public CuentaResponse actualizar(String numero, CuentaRequest req) {
        var c = repo.findByNumero(numero).orElseThrow(() -> new IllegalArgumentException("Cuenta no existe"));
        c.setTipo(req.getTipo());
        c.setClienteId(req.getClienteId());
        c.setClienteNombre(req.getClienteNombre());
        return mapper.toResponse(repo.save(c));
    }

    public void eliminar(String numero) {
        repo.findByNumero(numero)
                .orElseThrow(() -> new IllegalArgumentException("Cuenta no existe"));
        if (movRepo.existsByCuentaNumero(numero)) {
            throw new IllegalStateException("No puede eliminar una cuenta con movimientos");
        }
        repo.deleteByNumero(numero);
    }
}