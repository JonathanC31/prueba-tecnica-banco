package com.jonathan.banco.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder(toBuilder = true)
public class Transaccion {
    private final Long id;
    private final TipoTransaccion tipo;
    private final BigDecimal monto;
    private final Long cuentaOrigenId;    // null en consignación
    private final Long cuentaDestinoId;   // null en retiro
    private final LocalDateTime fecha;

    public static Transaccion crear(TipoTransaccion tipo, BigDecimal monto,
                                    Long origenId, Long destinoId) {
        return Transaccion.builder()
                .tipo(tipo).monto(monto)
                .cuentaOrigenId(origenId).cuentaDestinoId(destinoId)
                .fecha(LocalDateTime.now())
                .build();
    }
}