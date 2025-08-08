package com.sofka.account.api.mapper;

import com.sofka.account.api.dto.CuentaRequest;
import com.sofka.account.api.dto.CuentaResponse;
import com.sofka.account.domain.model.Cuenta;
import org.springframework.stereotype.Component;

@Component
public class CuentaMapper {

    public Cuenta toEntity(CuentaRequest r) {
        Cuenta c = new Cuenta();
        c.setNumero(r.getNumero());
        c.setTipo(r.getTipo());
        c.setSaldoInicial(r.getSaldoInicial());
        c.setEstado(true);
        c.setClienteId(r.getClienteId());
        c.setClienteNombre(r.getClienteNombre());
        return c;
    }

    public CuentaResponse toResponse(Cuenta c) {
        CuentaResponse r = new CuentaResponse();
        r.setNumero(c.getNumero());
        r.setTipo(c.getTipo());
        r.setSaldoInicial(c.getSaldoInicial());
        r.setEstado(c.getEstado());
        r.setClienteId(c.getClienteId());
        r.setClienteNombre(c.getClienteNombre());
        return r;
    }
}
