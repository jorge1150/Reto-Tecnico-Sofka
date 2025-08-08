package com.sofka.customer.api.controller;

import com.sofka.customer.api.dto.ClienteRequest;
import com.sofka.customer.api.dto.ClienteResponse;
import com.sofka.customer.aplication.service.ClienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService service;

    @GetMapping
    public List<ClienteResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{clienteId}")
    public ClienteResponse obtener(@PathVariable String clienteId) {
        return service.obtenerPorClienteId(clienteId);
    }

    @GetMapping("/id/{id}")
    public ClienteResponse obtenerPorId(@PathVariable Long id) {
        return service.obtenerPorId(id);
    }

    @PostMapping
    public ResponseEntity<ClienteResponse> crear(@Valid @RequestBody ClienteRequest request) {
        var created = service.crear(request);
        var location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{clienteId}")
                .buildAndExpand(created.getClienteId()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/id/{id}")
    public ClienteResponse actualizar(@PathVariable Long id, @Valid @RequestBody ClienteRequest request) {
        return service.actualizar(id, request);
    }

    @PutMapping("/desactivar/id/{id}")
    public ResponseEntity<Void> desactivar(@PathVariable Long id) {
        service.desactivar(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/id/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}