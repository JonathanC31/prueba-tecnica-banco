package com.jonathan.banco.application.service;

import com.jonathan.banco.application.port.in.*;
import com.jonathan.banco.application.port.out.IClienteRepositoryPort;
import com.jonathan.banco.application.port.out.ICuentaRepositoryPort;
import com.jonathan.banco.domain.exception.RecursoNoEncontradoException;
import com.jonathan.banco.domain.exception.ReglaNegocioException;
import com.jonathan.banco.domain.model.Cliente;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClienteService implements ICrearClienteUseCase, IActualizarClienteUseCase,
        IEliminarClienteUseCase, IConsultarClienteUseCase {

    private final IClienteRepositoryPort clienteRepository;
    private final ICuentaRepositoryPort cuentaRepository;

    @Override
    @Transactional
    public Cliente crear(CrearClienteCommand command) {
        if (clienteRepository.existePorNumeroIdentificacion(command.numeroIdentificacion())) {
            throw new ReglaNegocioException("Ya existe un cliente con ese número de identificación");
        }
        Cliente cliente = Cliente.crear(
                command.tipoIdentificacion(), command.numeroIdentificacion(),
                command.nombres(), command.apellido(),
                command.correo(), command.fechaNacimiento());
        Cliente guardado = clienteRepository.guardar(cliente);
        log.info("Cliente creado id={}", guardado.getId());
        return guardado;
    }

    @Override
    @Transactional
    public Cliente actualizar(Long id, ActualizarClienteCommand command) {
        Cliente existente = buscarPorId(id);
        Cliente actualizado = existente.actualizar(
                command.nombres(), command.apellido(),
                command.correo(), command.fechaNacimiento());
        return clienteRepository.guardar(actualizado);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        buscarPorId(id);   // lanza 404 si no existe
        if (cuentaRepository.existePorClienteId(id)) {
            throw new ReglaNegocioException("No se puede eliminar un cliente con productos vinculados");
        }
        clienteRepository.eliminar(id);
        log.info("Cliente eliminado id={}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Cliente buscarPorId(Long id) {
        return clienteRepository.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe un cliente con id " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cliente> listar() {
        return clienteRepository.listar();
    }
}