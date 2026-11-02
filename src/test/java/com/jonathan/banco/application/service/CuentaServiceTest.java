package com.jonathan.banco.application.service;

import com.jonathan.banco.application.port.in.CrearCuentaCommand;
import com.jonathan.banco.application.port.out.IClienteRepositoryPort;
import com.jonathan.banco.application.port.out.ICuentaRepositoryPort;
import com.jonathan.banco.domain.exception.RecursoNoEncontradoException;
import com.jonathan.banco.domain.exception.ReglaNegocioException;
import com.jonathan.banco.domain.model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CuentaServiceTest {

    @Mock
    private ICuentaRepositoryPort cuentaRepository;

    @Mock
    private IClienteRepositoryPort clienteRepository;

    @InjectMocks
    private CuentaService cuentaService;

    private Cliente clienteValido() {
        return Cliente.crear(
                TipoIdentificacion.CC, "1020304050", "Jonathan", "Cortes",
                "jonathan@banco.com", LocalDate.of(1998, 5, 15)
        ).toBuilder().id(1L).build();
    }

    @Test
    @DisplayName("Debe crear cuenta de ahorros activa con prefijo 53 y saldo cero")
    void crearCuentaAhorrosExitoso() {
        CrearCuentaCommand command = new CrearCuentaCommand(TipoCuenta.AHORROS, 1L, true);
        when(clienteRepository.buscarPorId(1L)).thenReturn(Optional.of(clienteValido()));
        when(cuentaRepository.existePorNumeroCuenta(anyString())).thenReturn(false);
        when(cuentaRepository.guardar(any(Cuenta.class)))
                .thenAnswer(inv -> inv.<Cuenta>getArgument(0).toBuilder().id(10L).build());

        Cuenta creada = cuentaService.crear(command);

        assertNotNull(creada);
        assertEquals(10L, creada.getId());
        assertTrue(creada.getNumeroCuenta().startsWith("53"));
        assertEquals(10, creada.getNumeroCuenta().length());
        assertEquals(EstadoCuenta.ACTIVA, creada.getEstado());
        assertEquals(BigDecimal.ZERO, creada.getSaldo());
        assertTrue(creada.isExentaGmf());
    }

    @Test
    @DisplayName("Debe crear cuenta corriente con prefijo 33")
    void crearCuentaCorrienteExitoso() {
        CrearCuentaCommand command = new CrearCuentaCommand(TipoCuenta.CORRIENTE, 1L, false);
        when(clienteRepository.buscarPorId(1L)).thenReturn(Optional.of(clienteValido()));
        when(cuentaRepository.existePorNumeroCuenta(anyString())).thenReturn(false);
        when(cuentaRepository.guardar(any(Cuenta.class)))
                .thenAnswer(inv -> inv.<Cuenta>getArgument(0).toBuilder().id(11L).build());

        Cuenta creada = cuentaService.crear(command);

        assertTrue(creada.getNumeroCuenta().startsWith("33"));
        assertEquals(TipoCuenta.CORRIENTE, creada.getTipo());
    }

    @Test
    @DisplayName("Debe lanzar RecursoNoEncontradoException si el cliente no existe al crear cuenta")
    void crearCuentaClienteNoExisteLanzaExcepcion() {
        CrearCuentaCommand command = new CrearCuentaCommand(TipoCuenta.AHORROS, 99L, false);
        when(clienteRepository.buscarPorId(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> cuentaService.crear(command));
        verify(cuentaRepository, never()).guardar(any());
    }

    @Test
    @DisplayName("Debe inactivar y luego reactivar una cuenta activa")
    void inactivarYActivarCuentaExitoso() {
        Cuenta activa = Cuenta.crear(TipoCuenta.AHORROS, 1L, false, "5300000001")
                .toBuilder().id(10L).build();
        when(cuentaRepository.buscarPorId(10L)).thenReturn(Optional.of(activa));
        when(cuentaRepository.guardar(any(Cuenta.class))).thenAnswer(inv -> inv.getArgument(0));

        Cuenta inactiva = cuentaService.inactivar(10L);
        assertEquals(EstadoCuenta.INACTIVA, inactiva.getEstado());

        when(cuentaRepository.buscarPorId(10L)).thenReturn(Optional.of(inactiva));
        Cuenta reactivada = cuentaService.activar(10L);
        assertEquals(EstadoCuenta.ACTIVA, reactivada.getEstado());
    }

    @Test
    @DisplayName("Debe cancelar una cuenta cuando su saldo es cero")
    void cancelarCuentaConSaldoCeroExitoso() {
        Cuenta cuenta = Cuenta.crear(TipoCuenta.AHORROS, 1L, false, "5300000001")
                .toBuilder().id(10L).build();
        when(cuentaRepository.buscarPorId(10L)).thenReturn(Optional.of(cuenta));
        when(cuentaRepository.guardar(any(Cuenta.class))).thenAnswer(inv -> inv.getArgument(0));

        Cuenta cancelada = cuentaService.cancelar(10L);

        assertEquals(EstadoCuenta.CANCELADA, cancelada.getEstado());
        assertThrows(ReglaNegocioException.class, cancelada::activar);
    }

    @Test
    @DisplayName("Debe lanzar ReglaNegocioException al intentar cancelar cuenta con saldo mayor a cero")
    void cancelarCuentaConSaldoMayorACeroLanzaExcepcion() {
        Cuenta conSaldo = Cuenta.crear(TipoCuenta.AHORROS, 1L, false, "5300000001")
                .depositar(new BigDecimal("50000"))
                .toBuilder().id(10L).build();
        when(cuentaRepository.buscarPorId(10L)).thenReturn(Optional.of(conSaldo));

        assertThrows(ReglaNegocioException.class, () -> cuentaService.cancelar(10L));
        verify(cuentaRepository, never()).guardar(any());
    }

    @Test
    @DisplayName("Debe buscar por id y listar cuentas por cliente")
    void buscarYListarCuentasExitoso() {
        Cuenta cuenta = Cuenta.crear(TipoCuenta.AHORROS, 1L, false, "5300000001")
                .toBuilder().id(10L).build();
        when(cuentaRepository.buscarPorId(10L)).thenReturn(Optional.of(cuenta));
        when(cuentaRepository.listarPorClienteId(1L)).thenReturn(List.of(cuenta));

        assertEquals("5300000001", cuentaService.buscarPorId(10L).getNumeroCuenta());
        assertEquals(1, cuentaService.listarPorClienteId(1L).size());
    }
}
