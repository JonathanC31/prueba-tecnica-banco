package com.jonathan.banco.application.port.in;

import com.jonathan.banco.domain.model.TipoTransaccion;

import java.math.BigDecimal;

public record EjecutarTransaccionCommand(
        TipoTransaccion tipo,
        BigDecimal monto,
        Long cuentaOrigenId,
        Long cuentaDestinoId) {}