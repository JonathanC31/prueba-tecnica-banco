package com.jonathan.banco.infrastructure.adapter.in.web;

import com.jonathan.banco.domain.model.Cuenta;
import com.jonathan.banco.domain.model.EstadoCuenta;
import com.jonathan.banco.domain.model.TipoCuenta;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CuentaResponse(
        Long id,
        String numeroCuenta,
        TipoCuenta tipo,
        EstadoCuenta estado,
        BigDecimal saldo,
        boolean exentaGmf,
        Long clienteId,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaModificacion) {

    public static CuentaResponse desde(Cuenta c) {
        return new CuentaResponse(c.getId(), c.getNumeroCuenta(), c.getTipo(),
                c.getEstado(), c.getSaldo(), c.isExentaGmf(), c.getClienteId(),
                c.getFechaCreacion(), c.getFechaModificacion());
    }
}