package com.jonathan.banco.application.port.in;

import com.jonathan.banco.domain.model.TipoCuenta;

public record CrearCuentaCommand(
        TipoCuenta tipo,
        Long clienteId,
        boolean exentaGmf) {}