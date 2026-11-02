package com.jonathan.banco.domain.model;

import com.jonathan.banco.domain.exception.ReglaNegocioException;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder(toBuilder = true)
public class Cuenta {

    private final Long id;
    private final String numeroCuenta;
    private final TipoCuenta tipo;
    private final EstadoCuenta estado;
    private final BigDecimal saldo;
    private final boolean exentaGmf;
    private final Long clienteId;
    private final LocalDateTime fechaCreacion;
    private final LocalDateTime fechaModificacion;

    public static Cuenta crear(TipoCuenta tipo, Long clienteId,
                               boolean exentaGmf, String numeroCuenta) {
        return Cuenta.builder()
                .numeroCuenta(numeroCuenta)
                .tipo(tipo)
                .estado(EstadoCuenta.ACTIVA)
                .saldo(BigDecimal.ZERO)
                .exentaGmf(exentaGmf)
                .clienteId(clienteId)
                .fechaCreacion(LocalDateTime.now())
                .fechaModificacion(LocalDateTime.now())
                .build();
    }

    public Cuenta activar() {
        validarNoCancelada();
        return cambiarEstado(EstadoCuenta.ACTIVA);
    }

    public Cuenta inactivar() {
        validarNoCancelada();
        return cambiarEstado(EstadoCuenta.INACTIVA);
    }

    public Cuenta cancelar() {
        if (saldo.compareTo(BigDecimal.ZERO) != 0) {
            throw new ReglaNegocioException("Solo se puede cancelar una cuenta con saldo 0");
        }
        return cambiarEstado(EstadoCuenta.CANCELADA);
    }

    private void validarNoCancelada() {
        if (estado == EstadoCuenta.CANCELADA) {
            throw new ReglaNegocioException("Una cuenta cancelada no puede cambiar de estado");
        }
    }

    private Cuenta cambiarEstado(EstadoCuenta nuevo) {
        return this.toBuilder()
                .estado(nuevo)
                .fechaModificacion(LocalDateTime.now())
                .build();
    }
    public Cuenta depositar(BigDecimal monto) {
        validarMontoPositivo(monto);
        validarActiva();
        return this.toBuilder()
                .saldo(saldo.add(monto))
                .fechaModificacion(LocalDateTime.now())
                .build();
    }

    public Cuenta retirar(BigDecimal monto) {
        validarMontoPositivo(monto);
        validarActiva();
        if (saldo.compareTo(monto) < 0) {
            throw new ReglaNegocioException("Saldo insuficiente");
        }
        return this.toBuilder()
                .saldo(saldo.subtract(monto))
                .fechaModificacion(LocalDateTime.now())
                .build();
    }

    private void validarActiva() {
        if (estado != EstadoCuenta.ACTIVA) {
            throw new ReglaNegocioException("La cuenta debe estar activa para operar");
        }
    }

    private void validarMontoPositivo(BigDecimal monto) {
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ReglaNegocioException("El monto debe ser mayor a cero");
        }
    }
}