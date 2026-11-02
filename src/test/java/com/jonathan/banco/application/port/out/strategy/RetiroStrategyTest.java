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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RetiroStrategyTest {

    @Mock
    private ICuentaRepositoryPort cuentaRepository;

    @Mock
    private ITransaccionRepositoryPort transaccionRepository;

    @InjectMocks
    private RetiroStrategy strategy;

    @Test
    @DisplayName("Debe descontar el saldo de la cuenta origen y registrar el retiro")
    void retiroExitoso() {
        Cuenta origen = Cuenta.crear(TipoCuenta.AHORROS, 1L, false, "5300000001")
                .depositar(new BigDecimal("200000"))
                .toBuilder().id(10L).build();
        EjecutarTransaccionCommand command = new EjecutarTransaccionCommand(
                TipoTransaccion.RETIRO, new BigDecimal("80000"), 10L, null
        );
        when(cuentaRepository.buscarPorId(10L)).thenReturn(Optional.of(origen));
        when(transaccionRepository.guardar(any(Transaccion.class)))
                .thenAnswer(inv -> inv.<Transaccion>getArgument(0).toBuilder().id(101L).build());

        Transaccion resultado = strategy.ejecutar(command);

        ArgumentCaptor<Cuenta> captor = ArgumentCaptor.forClass(Cuenta.class);
        verify(cuentaRepository).guardar(captor.capture());
        assertEquals(new BigDecimal("120000"), captor.getValue().getSaldo());
        assertEquals(101L, resultado.getId());
        assertEquals(TipoTransaccion.RETIRO, strategy.tipo());
    }

    @Test
    @DisplayName("Debe lanzar ReglaNegocioException cuando el saldo es insuficiente")
    void retiroSaldoInsuficienteLanzaExcepcion() {
        Cuenta origen = Cuenta.crear(TipoCuenta.AHORROS, 1L, false, "5300000001")
                .depositar(new BigDecimal("30000"))
                .toBuilder().id(10L).build();
        EjecutarTransaccionCommand command = new EjecutarTransaccionCommand(
                TipoTransaccion.RETIRO, new BigDecimal("50000"), 10L, null
        );
        when(cuentaRepository.buscarPorId(10L)).thenReturn(Optional.of(origen));

        assertThrows(ReglaNegocioException.class, () -> strategy.ejecutar(command));
        verify(transaccionRepository, never()).guardar(any());
    }

    @Test
    @DisplayName("Debe lanzar ReglaNegocioException si cuentaOrigenId es null")
    void retiroSinOrigenLanzaExcepcion() {
        EjecutarTransaccionCommand command = new EjecutarTransaccionCommand(
                TipoTransaccion.RETIRO, new BigDecimal("50000"), null, null
        );

        assertThrows(ReglaNegocioException.class, () -> strategy.ejecutar(command));
    }
}
