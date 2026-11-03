package com.jonathan.banco.infrastructure.adapter.in.web;

import com.jonathan.banco.application.port.in.*;
import com.jonathan.banco.domain.model.Cliente;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ICrearClienteUseCase crearCliente;
    private final IActualizarClienteUseCase actualizarCliente;
    private final IEliminarClienteUseCase eliminarCliente;
    private final IConsultarClienteUseCase consultarCliente;

    @PostMapping
    public ResponseEntity<ClienteResponse> crear(@Valid @RequestBody ClienteRequest r) {
        Cliente creado = crearCliente.crear(new CrearClienteCommand(
                r.tipoIdentificacion(), r.numeroIdentificacion(), r.nombres(),
                r.apellido(), r.correo(), r.fechaNacimiento()));
        return ResponseEntity.status(HttpStatus.CREATED).body(ClienteResponse.from(creado));
    }

    @GetMapping("/{id}")
    public ClienteResponse buscar(@PathVariable Long id) {
        return ClienteResponse.from(consultarCliente.buscarPorId(id));
    }

    @GetMapping
    public List<ClienteResponse> listar() {
        return consultarCliente.listar().stream().map(ClienteResponse::from).toList();
    }

    @PutMapping("/{id}")
    public ClienteResponse actualizar(@PathVariable Long id,
                                      @Valid @RequestBody ClienteUpdateRequest r) {
        return ClienteResponse.from(actualizarCliente.actualizar(id,
                new ActualizarClienteCommand(r.nombres(), r.apellido(), r.correo(), r.fechaNacimiento())));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        eliminarCliente.eliminar(id);
    }
}