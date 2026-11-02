package com.jonathan.banco.application.service;

import com.jonathan.banco.application.port.in.ActualizarClienteCommand;
import com.jonathan.banco.application.port.in.CrearClienteCommand;
import com.jonathan.banco.application.port.out.IClienteRepositoryPort;
import com.jonathan.banco.application.port.out.ICuentaRepositoryPort;
import com.jonathan.banco.domain.exception.RecursoNoEncontradoException;
import com.jonathan.banco.domain.exception.ReglaNegocioException;
import com.jonathan.banco.domain.model.Cliente;
import com.jonathan.banco.domain.model.TipoIdentificacion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private IClienteRepositoryPort clienteRepository;

    @Mock
    private ICuentaRepositoryPort cuentaRepository;

    @InjectMocks
    private ClienteService clienteService;

    private Cliente clienteBase() {
        return Cliente.crear(
                TipoIdentificacion.CC,
                "1020304050",
                "Jonathan",
                "Cortes",
                "jonathan@banco.com",
                LocalDate.of(1998, 5, 15)
        ).toBuilder().id(1L).build();
    }

    @Test
    @DisplayName("Debe crear un cliente mayor de edad cuando no existe su identificacion")
    void crearClienteExitoso() {
        CrearClienteCommand command = new CrearClienteCommand(
                TipoIdentificacion.CC, "1020304050", "Jonathan", "Cortes",
                "jonathan@banco.com", LocalDate.of(1998, 5, 15)
        );
        when(clienteRepository.existePorNumeroIdentificacion("1020304050")).thenReturn(false);
        when(clienteRepository.guardar(any(Cliente.class))).thenReturn(clienteBase());

        Cliente resultado = clienteService.crear(command);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Jonathan", resultado.getNombres());
        verify(clienteRepository).guardar(any(Cliente.class));
    }

    @Test
    @DisplayName("Debe lanzar ReglaNegocioException al crear cliente con identificacion duplicada")
    void crearClienteDuplicadoLanzaExcepcion() {
        CrearClienteCommand command = new CrearClienteCommand(
                TipoIdentificacion.CC, "1020304050", "Jonathan", "Cortes",
                "jonathan@banco.com", LocalDate.of(1998, 5, 15)
        );
        when(clienteRepository.existePorNumeroIdentificacion("1020304050")).thenReturn(true);

        assertThrows(ReglaNegocioException.class, () -> clienteService.crear(command));
        verify(clienteRepository, never()).guardar(any());
    }

    @Test
    @DisplayName("Debe lanzar ReglaNegocioException al crear cliente menor de edad")
    void crearClienteMenorDeEdadLanzaExcepcion() {
        CrearClienteCommand command = new CrearClienteCommand(
                TipoIdentificacion.TI, "99887766", "Menor", "Apellido",
                "menor@banco.com", LocalDate.now().minusYears(16)
        );
        when(clienteRepository.existePorNumeroIdentificacion("99887766")).thenReturn(false);

        assertThrows(ReglaNegocioException.class, () -> clienteService.crear(command));
        verify(clienteRepository, never()).guardar(any());
    }

    @Test
    @DisplayName("Debe actualizar datos y fechaModificacion de un cliente existente")
    void actualizarClienteExitoso() {
        Cliente existente = clienteBase();
        ActualizarClienteCommand command = new ActualizarClienteCommand(
                "Jonathan Andres", "Cortes", "nuevo@banco.com", LocalDate.of(1998, 5, 15)
        );
        when(clienteRepository.buscarPorId(1L)).thenReturn(Optional.of(existente));
        when(clienteRepository.guardar(any(Cliente.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Cliente actualizado = clienteService.actualizar(1L, command);

        assertEquals("Jonathan Andres", actualizado.getNombres());
        assertEquals("nuevo@banco.com", actualizado.getCorreo());
        assertNotNull(actualizado.getFechaModificacion());
        verify(clienteRepository).guardar(any(Cliente.class));
    }

    @Test
    @DisplayName("Debe lanzar RecursoNoEncontradoException al buscar cliente inexistente")
    void buscarPorIdNoExistenteLanzaExcepcion() {
        when(clienteRepository.buscarPorId(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> clienteService.buscarPorId(99L));
    }

    @Test
    @DisplayName("Debe listar todos los clientes")
    void listarClientesExitoso() {
        when(clienteRepository.listar()).thenReturn(List.of(clienteBase()));

        List<Cliente> lista = clienteService.listar();

        assertEquals(1, lista.size());
        verify(clienteRepository).listar();
    }

    @Test
    @DisplayName("Debe eliminar un cliente cuando no tiene cuentas vinculadas")
    void eliminarClienteSinCuentasExitoso() {
        when(clienteRepository.buscarPorId(1L)).thenReturn(Optional.of(clienteBase()));
        when(cuentaRepository.existePorClienteId(1L)).thenReturn(false);

        clienteService.eliminar(1L);

        verify(clienteRepository).eliminar(1L);
    }

    @Test
    @DisplayName("Debe lanzar ReglaNegocioException al intentar eliminar cliente con cuentas vinculadas")
    void eliminarClienteConCuentasLanzaExcepcion() {
        when(clienteRepository.buscarPorId(1L)).thenReturn(Optional.of(clienteBase()));
        when(cuentaRepository.existePorClienteId(1L)).thenReturn(true);

        assertThrows(ReglaNegocioException.class, () -> clienteService.eliminar(1L));
        verify(clienteRepository, never()).eliminar(anyLong());
    }
}
