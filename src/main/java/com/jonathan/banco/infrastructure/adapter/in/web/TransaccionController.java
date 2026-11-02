package com.jonathan.banco.infrastructure.adapter.in.web;

import com.jonathan.banco.application.port.in.EjecutarTransaccionCommand;
import com.jonathan.banco.application.port.in.IConsultarTransaccionUseCase;
import com.jonathan.banco.application.port.in.IRegistrarTransaccionUseCase;
import com.jonathan.banco.domain.model.Transaccion;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transacciones")
@RequiredArgsConstructor
public class TransaccionController {

    private final IRegistrarTransaccionUseCase registrar;
    private final IConsultarTransaccionUseCase consultar;

    @PostMapping
    public ResponseEntity<TransaccionResponse> ejecutar(@Valid @RequestBody TransaccionRequest r) {
        Transaccion t = registrar.ejecutar(new EjecutarTransaccionCommand(
                r.tipo(), r.monto(), r.cuentaOrigenId(), r.cuentaDestinoId()));
        return ResponseEntity.status(HttpStatus.CREATED).body(TransaccionResponse.desde(t));
    }

    @GetMapping
    public List<TransaccionResponse> listar(@RequestParam Long cuentaId) {
        return consultar.listarPorCuentaId(cuentaId).stream()
                .map(TransaccionResponse::desde).toList();
    }
}