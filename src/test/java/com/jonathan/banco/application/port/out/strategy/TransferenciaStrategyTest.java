package com.jonathan.banco.application.port.out.strategy;

import com.jonathan.banco.application.port.in.EjecutarTransaccionCommand;
import com.jonathan.banco.application.port.out.ICuentaRepositoryPort;
import com.jonathan.banco.application.port.out.ITransaccionRepositoryPort;
import com.jonathan.banco.domain.exception.ReglaNegocioException;
import com.jonathan.banco.domain.model.Cuenta;
import com.jonathan.banco.domain.model.TipoCuenta;
import com.jonathan.banco.domain.model.TipoTransaccion;
import com.jonathan.banco.domain.model.Transaccion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferenciaStrategyTest {

    @Mock
    private ICuentaRepositoryPort cuentaRepository;

    @Mock
    private ITransaccionRepositoryPort transaccionRepository;

    @InjectMocks
    private TransferenciaStrategy strategy;

    @Test
    @DisplayName("Debe debitar cuenta origen, acreditar cuenta destino y guardar transaccion")
    void transferenciaExitosa() {
        Cuenta origen = Cuenta.crear(TipoCuenta.AHORROS, 1L, false, "5300000001")
                .depositar(new BigDecimal("300000"))
                .toBuilder().id(10L).build();
        Cuenta destino = Cuenta.crear(TipoCuenta.CORRIENTE, 2L, false, "3300000002")
                .toBuilder().id(20L).build();

        EjecutarTransaccionCommand command = new EjecutarTransaccionCommand(
                TipoTransaccion.TRANSFERENCIA, new BigDecimal("100000"), 10L, 20L
        );
        when(cuentaRepository.buscarPorId(10L)).thenReturn(Optional.of(origen));
        when(cuentaRepository.buscarPorId(20L)).thenReturn(Optional.of(destino));
        when(transaccionRepository.guardar(any(Transaccion.class)))
                .thenAnswer(inv -> inv.<Transaccion>getArgument(0).toBuilder().id(102L).build());

        Transaccion resultado = strategy.ejecutar(command);

        ArgumentCaptor<Cuenta> captor = ArgumentCaptor.forClass(Cuenta.class);
        verify(cuentaRepository, times(2)).guardar(captor.capture());
        List<Cuenta> guardadas = captor.getAllValues();
        assertEquals(new BigDecimal("200000"), guardadas.get(0).getSaldo());
        assertEquals(new BigDecimal("100000"), guardadas.get(1).getSaldo());
        assertEquals(102L, resultado.getId());
        assertEquals(TipoTransaccion.TRANSFERENCIA, strategy.tipo());
    }

    @Test
    @DisplayName("Debe lanzar ReglaNegocioException cuando origen y destino son la misma cuenta")
    void transferenciaMismaCuentaLanzaExcepcion() {
        EjecutarTransaccionCommand command = new EjecutarTransaccionCommand(
                TipoTransaccion.TRANSFERENCIA, new BigDecimal("50000"), 10L, 10L
        );

        assertThrows(ReglaNegocioException.class, () -> strategy.ejecutar(command));
        verify(cuentaRepository, never()).guardar(any());
    }

    @Test
    @DisplayName("Debe lanzar ReglaNegocioException si falta origen o destino")
    void transferenciaSinCuentasLanzaExcepcion() {
        EjecutarTransaccionCommand sinOrigen = new EjecutarTransaccionCommand(
                TipoTransaccion.TRANSFERENCIA, new BigDecimal("50000"), null, 20L
        );
        EjecutarTransaccionCommand sinDestino = new EjecutarTransaccionCommand(
                TipoTransaccion.TRANSFERENCIA, new BigDecimal("50000"), 10L, null
        );

        assertThrows(ReglaNegocioException.class, () -> strategy.ejecutar(sinOrigen));
        assertThrows(ReglaNegocioException.class, () -> strategy.ejecutar(sinDestino));
    }
}
