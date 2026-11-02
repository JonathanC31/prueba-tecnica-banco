package com.jonathan.banco.infrastructure.adapter.in.web;

import com.jonathan.banco.domain.model.TipoCuenta;
import jakarta.validation.constraints.NotNull;

public record CuentaRequest(
        @NotNull(message = "El tipo de cuenta es obligatorio") TipoCuenta tipo,
        @NotNull(message = "El cliente es obligatorio") Long clienteId,
        boolean exentaGmf) {}