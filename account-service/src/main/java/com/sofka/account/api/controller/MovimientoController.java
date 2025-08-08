package com.sofka.account.api.controller;

import com.sofka.account.api.dto.MovimientoRequest;
import com.sofka.account.api.dto.MovimientoResponse;
import com.sofka.account.api.dto.MovimientoUpdateRequest;
import com.sofka.account.aplication.service.MovimientoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/movimientos")
@RequiredArgsConstructor
public class MovimientoController {

    private final MovimientoService service;

    @PostMapping
    public ResponseEntity<MovimientoResponse> registrar(
            @RequestParam String cuenta,
            @Valid @RequestBody MovimientoRequest request) {

        MovimientoResponse created = service.registrar(cuenta, request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/{id}")
    public MovimientoResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @GetMapping
    public List<MovimientoResponse> listar(@RequestParam(required = false) String cuenta) {
        return service.listarPorCuenta(cuenta);
    }

    @PutMapping("/{id}")
    public MovimientoResponse actualizar(@PathVariable Long id, @Valid @RequestBody MovimientoUpdateRequest req) {
        return service.actualizar(id, req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }

}
