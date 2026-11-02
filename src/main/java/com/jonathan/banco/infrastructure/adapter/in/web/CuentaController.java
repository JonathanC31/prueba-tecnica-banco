package com.jonathan.banco.infrastructure.adapter.in.web;

import com.jonathan.banco.application.port.in.CrearCuentaCommand;
import com.jonathan.banco.application.port.in.ICambiarEstadoCuentaUseCase;
import com.jonathan.banco.application.port.in.IConsultarCuentaUseCase;
import com.jonathan.banco.application.port.in.ICrearCuentaUseCase;
import com.jonathan.banco.domain.model.Cuenta;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cuentas")
@RequiredArgsConstructor
public class CuentaController {

    private final ICrearCuentaUseCase crearCuenta;
    private final ICambiarEstadoCuentaUseCase cambiarEstado;
    private final IConsultarCuentaUseCase consultarCuenta;

    @PostMapping
    public ResponseEntity<CuentaResponse> crear(@Valid @RequestBody CuentaRequest request) {
        Cuenta cuenta = crearCuenta.crear(new CrearCuentaCommand(
                request.tipo(), request.clienteId(), request.exentaGmf()));
        return ResponseEntity.status(HttpStatus.CREATED).body(CuentaResponse.desde(cuenta));
    }

    @GetMapping("/{id}")
    public CuentaResponse buscar(@PathVariable Long id) {
        return CuentaResponse.desde(consultarCuenta.buscarPorId(id));
    }

    @GetMapping
    public List<CuentaResponse> listarPorCliente(@RequestParam Long clienteId) {
        return consultarCuenta.listarPorClienteId(clienteId).stream()
                .map(CuentaResponse::desde)
                .toList();
    }

    @PatchMapping("/{id}/activar")
    public CuentaResponse activar(@PathVariable Long id) {
        return CuentaResponse.desde(cambiarEstado.activar(id));
    }

    @PatchMapping("/{id}/inactivar")
    public CuentaResponse inactivar(@PathVariable Long id) {
        return CuentaResponse.desde(cambiarEstado.inactivar(id));
    }

    @PatchMapping("/{id}/cancelar")
    public CuentaResponse cancelar(@PathVariable Long id) {
        return CuentaResponse.desde(cambiarEstado.cancelar(id));
    }
}