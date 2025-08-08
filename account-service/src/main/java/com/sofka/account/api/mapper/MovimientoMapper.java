package com.sofka.account.api.mapper;

import com.sofka.account.api.dto.EstadoCuentaResponse;
import com.sofka.account.api.dto.MovimientoRequest;
import com.sofka.account.api.dto.MovimientoResponse;
import com.sofka.account.domain.model.Movimiento;
import org.springframework.stereotype.Component;

@Component
public class MovimientoMapper {

    public Movimiento toEntity(MovimientoRequest r) {
        Movimiento m = new Movimiento();
        m.setFecha(r.getFecha());
        m.setTipo(r.getTipo());
        m.setValor(r.getValor());
        return m;
    }

    public MovimientoResponse toResponse(Movimiento m) {
        MovimientoResponse r = new MovimientoResponse();
        r.setId(m.getId());
        r.setFecha(m.getFecha());
        r.setTipo(m.getTipo());
        r.setValor(m.getValor());
        r.setSaldo(m.getSaldo());
        r.setCuentaNumero(m.getCuenta().getNumero());
        return r;
    }

    /** Mapea una línea del estado de cuenta. */
    public EstadoCuentaResponse toEstado(Movimiento m) {
        EstadoCuentaResponse r = new EstadoCuentaResponse();
        r.setFecha(m.getFecha());
        r.setClienteNombre(m.getCuenta().getClienteNombre());
        r.setCuentaNumero(m.getCuenta().getNumero());
        r.setTipoCuenta(m.getCuenta().getTipo());
        r.setTipo(m.getTipo());
        r.setValor(m.getValor());
        r.setSaldo(m.getSaldo());
        r.setClienteId(m.getCuenta().getClienteId());
        return r;
    }
}
