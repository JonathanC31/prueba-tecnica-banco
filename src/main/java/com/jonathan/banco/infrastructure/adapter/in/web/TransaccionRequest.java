package com.jonathan.banco.infrastructure.adapter.in.web;

import com.jonathan.banco.domain.model.TipoTransaccion;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record TransaccionRequest(
        @NotNull(message = "El tipo es obligatorio") TipoTransaccion tipo,
        @NotNull(message = "El monto es obligatorio")
        @Positive(message = "El monto debe ser mayor a cero") BigDecimal monto,
        Long cuentaOrigenId,
        Long cuentaDestinoId) {}