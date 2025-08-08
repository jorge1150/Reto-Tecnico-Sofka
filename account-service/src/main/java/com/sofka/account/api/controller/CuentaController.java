package com.sofka.account.api.controller;

import com.sofka.account.api.dto.CuentaRequest;
import com.sofka.account.api.dto.CuentaResponse;
import com.sofka.account.aplication.service.CuentaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/cuentas")
@RequiredArgsConstructor
public class CuentaController {

    private final CuentaService service;

    @PostMapping
    public ResponseEntity<CuentaResponse> crear(@Valid @RequestBody CuentaRequest request) {
        CuentaResponse created = service.crear(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{numero}")
                .buildAndExpand(created.getNumero())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping
    public List<CuentaResponse> listarPorCliente(@RequestParam String clienteId) {
        return service.listarPorCliente(clienteId);
    }

    @GetMapping("/{numero}")
    public CuentaResponse obtener(@PathVariable String numero) {
        return service.obtener(numero);
    }

    @PutMapping("/{numero}")
    public CuentaResponse actualizar(@PathVariable String numero, @Valid @RequestBody CuentaRequest request) {
        return service.actualizar(numero, request);
    }

    @DeleteMapping("/{numero}")
    public ResponseEntity<Void> eliminar(@PathVariable String numero) {
        service.eliminar(numero);
        return ResponseEntity.noContent().build();
    }
}