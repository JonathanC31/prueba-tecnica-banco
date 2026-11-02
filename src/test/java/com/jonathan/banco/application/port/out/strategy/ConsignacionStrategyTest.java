package com.jonathan.banco.application.port.out.strategy;

import com.jonathan.banco.application.port.in.EjecutarTransaccionCommand;
import com.jonathan.banco.application.port.out.ICuentaRepositoryPort;
import com.jonathan.banco.application.port.out.ITransaccionRepositoryPort;
import com.jonathan.banco.domain.exception.RecursoNoEncontradoException;
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
class ConsignacionStrategyTest {

    @Mock
    private ICuentaRepositoryPort cuentaRepository;

    @Mock
    private ITransaccionRepositoryPort transaccionRepository;

    @InjectMocks
    private ConsignacionStrategy strategy;

    @Test
    @DisplayName("Debe incrementar el saldo de la cuenta destino y registrar la consignacion")
    void consignacionExitosa() {
        Cuenta destino = Cuenta.crear(TipoCuenta.AHORROS, 1L, false, "5300000001")
                .toBuilder().id(10L).build();
        EjecutarTransaccionCommand command = new EjecutarTransaccionCommand(
                TipoTransaccion.CONSIGNACION, new BigDecimal("150000"), null, 10L
        );
        when(cuentaRepository.buscarPorId(10L)).thenReturn(Optional.of(destino));
        when(transaccionRepository.guardar(any(Transaccion.class)))
                .thenAnswer(inv -> inv.<Transaccion>getArgument(0).toBuilder().id(100L).build());

        Transaccion resultado = strategy.ejecutar(command);

        ArgumentCaptor<Cuenta> captor = ArgumentCaptor.forClass(Cuenta.class);
        verify(cuentaRepository).guardar(captor.capture());
        assertEquals(new BigDecimal("150000"), captor.getValue().getSaldo());
        assertEquals(100L, resultado.getId());
        assertEquals(TipoTransaccion.CONSIGNACION, strategy.tipo());
    }

    @Test
    @DisplayName("Debe lanzar ReglaNegocioException si cuentaDestinoId es null")
    void consignacionSinDestinoLanzaExcepcion() {
        EjecutarTransaccionCommand command = new EjecutarTransaccionCommand(
                TipoTransaccion.CONSIGNACION, new BigDecimal("150000"), null, null
        );

        assertThrows(ReglaNegocioException.class, () -> strategy.ejecutar(command));
    }

    @Test
    @DisplayName("Debe lanzar RecursoNoEncontradoException si la cuenta destino no existe")
    void consignacionCuentaNoExisteLanzaExcepcion() {
        EjecutarTransaccionCommand command = new EjecutarTransaccionCommand(
                TipoTransaccion.CONSIGNACION, new BigDecimal("150000"), null, 99L
        );
        when(cuentaRepository.buscarPorId(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> strategy.ejecutar(command));
    }
}
