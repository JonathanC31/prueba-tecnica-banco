package com.jonathan.banco.infrastructure.adapter.out.persistence;

import com.jonathan.banco.domain.model.Transaccion;
import org.springframework.stereotype.Component;

@Component
public class TransaccionMapper {

    public TransaccionEntity toEntity(Transaccion t) {
        return TransaccionEntity.builder()
                .id(t.getId())
                .tipo(t.getTipo())
                .monto(t.getMonto())
                .cuentaOrigenId(t.getCuentaOrigenId())
                .cuentaDestinoId(t.getCuentaDestinoId())
                .fecha(t.getFecha())
                .build();
    }

    public Transaccion toDomain(TransaccionEntity e) {
        return Transaccion.builder()
                .id(e.getId())
                .tipo(e.getTipo())
                .monto(e.getMonto())
                .cuentaOrigenId(e.getCuentaOrigenId())
                .cuentaDestinoId(e.getCuentaDestinoId())
                .fecha(e.getFecha())
                .build();
    }
}