package com.jonathan.banco.infrastructure.adapter.in.web;

import com.jonathan.banco.domain.model.TipoTransaccion;
import com.jonathan.banco.domain.model.Transaccion;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransaccionResponse(
        Long id, TipoTransaccion tipo, BigDecimal monto,
        Long cuentaOrigenId, Long cuentaDestinoId, LocalDateTime fecha) {

    public static TransaccionResponse desde(Transaccion t) {
        return new TransaccionResponse(t.getId(), t.getTipo(), t.getMonto(),
                t.getCuentaOrigenId(), t.getCuentaDestinoId(), t.getFecha());
    }
}