package com.jonathan.banco.application.service;

import com.jonathan.banco.application.port.in.EjecutarTransaccionCommand;
import com.jonathan.banco.application.port.out.ITransaccionRepositoryPort;
import com.jonathan.banco.application.port.out.strategy.ITransaccionStrategy;
import com.jonathan.banco.domain.model.TipoTransaccion;
import com.jonathan.banco.domain.model.Transaccion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransaccionServiceTest {

    @Mock
    private ITransaccionStrategy consignacionStrategy;

    @Mock
    private ITransaccionRepositoryPort transaccionRepository;

    private TransaccionService transaccionService;

    @BeforeEach
    void setUp() {
        when(consignacionStrategy.tipo()).thenReturn(TipoTransaccion.CONSIGNACION);
        transaccionService = new TransaccionService(List.of(consignacionStrategy), transaccionRepository);
    }

    @Test
    @DisplayName("Debe delegar la ejecucion a la estrategia correspondiente segun el tipo")
    void ejecutarDelegaAEstrategia() {
        EjecutarTransaccionCommand command = new EjecutarTransaccionCommand(
                TipoTransaccion.CONSIGNACION, new BigDecimal("100000"), null, 10L
        );
        Transaccion esperada = Transaccion.crear(
                TipoTransaccion.CONSIGNACION, new BigDecimal("100000"), null, 10L
        ).toBuilder().id(1L).build();
        when(consignacionStrategy.ejecutar(command)).thenReturn(esperada);

        Transaccion resultado = transaccionService.ejecutar(command);

        assertEquals(1L, resultado.getId());
        verify(consignacionStrategy).ejecutar(command);
    }

    @Test
    @DisplayName("Debe listar transacciones por cuentaId")
    void listarPorCuentaIdExitoso() {
        Transaccion t = Transaccion.crear(
                TipoTransaccion.CONSIGNACION, new BigDecimal("50000"), null, 10L
        ).toBuilder().id(1L).build();
        when(transaccionRepository.listarPorCuentaId(10L)).thenReturn(List.of(t));

        List<Transaccion> resultado = transaccionService.listarPorCuentaId(10L);

        assertEquals(1, resultado.size());
        verify(transaccionRepository).listarPorCuentaId(10L);
    }
}
