package com.sofka.account.aplication.service;

import com.sofka.account.api.dto.EstadoCuentaResponse;
import com.sofka.account.api.mapper.MovimientoMapper;
import com.sofka.account.domain.repository.MovimientoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReporteService {

    private final MovimientoRepository movRepo;
    private final MovimientoMapper mapper;

    public List<EstadoCuentaResponse> generar(String clienteId, LocalDate desde, LocalDate hasta) {
        return movRepo.findByClienteIdAndFechaBetween(clienteId, desde, hasta)
                .stream()
                .map(mapper::toEstado)
                .toList();
    }
}
