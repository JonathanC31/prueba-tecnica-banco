package com.jonathan.banco.infrastructure.adapter.out.persistence;

import com.jonathan.banco.domain.model.TipoTransaccion;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transacciones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransaccionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoTransaccion tipo;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal monto;

    @Column(name = "cuenta_origen_id")
    private Long cuentaOrigenId;

    @Column(name = "cuenta_destino_id")
    private Long cuentaDestinoId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fecha;
}