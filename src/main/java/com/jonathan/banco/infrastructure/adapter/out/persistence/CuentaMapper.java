package com.jonathan.banco.infrastructure.adapter.out.persistence;

import com.jonathan.banco.domain.model.Cuenta;
import org.springframework.stereotype.Component;

@Component
public class CuentaMapper {

    public CuentaEntity toEntity(Cuenta c) {
        return CuentaEntity.builder()
                .id(c.getId())
                .numeroCuenta(c.getNumeroCuenta())
                .tipo(c.getTipo())
                .estado(c.getEstado())
                .saldo(c.getSaldo())
                .exentaGmf(c.isExentaGmf())
                .clienteId(c.getClienteId())
                .fechaCreacion(c.getFechaCreacion())
                .fechaModificacion(c.getFechaModificacion())
                .build();
    }

    public Cuenta toDomain(CuentaEntity e) {
        return Cuenta.builder()
                .id(e.getId())
                .numeroCuenta(e.getNumeroCuenta())
                .tipo(e.getTipo())
                .estado(e.getEstado())
                .saldo(e.getSaldo())
                .exentaGmf(e.isExentaGmf())
                .clienteId(e.getClienteId())
                .fechaCreacion(e.getFechaCreacion())
                .fechaModificacion(e.getFechaModificacion())
                .build();
    }
}