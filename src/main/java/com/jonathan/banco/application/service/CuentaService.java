package com.jonathan.banco.application.service;

import com.jonathan.banco.application.port.in.CrearCuentaCommand;
import com.jonathan.banco.application.port.in.ICambiarEstadoCuentaUseCase;
import com.jonathan.banco.application.port.in.IConsultarCuentaUseCase;
import com.jonathan.banco.application.port.in.ICrearCuentaUseCase;
import com.jonathan.banco.application.port.out.IClienteRepositoryPort;
import com.jonathan.banco.application.port.out.ICuentaRepositoryPort;
import com.jonathan.banco.domain.exception.RecursoNoEncontradoException;
import com.jonathan.banco.domain.model.Cuenta;
import com.jonathan.banco.domain.model.TipoCuenta;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CuentaService implements ICrearCuentaUseCase,
        ICambiarEstadoCuentaUseCase, IConsultarCuentaUseCase {

    private final ICuentaRepositoryPort cuentaRepository;
    private final IClienteRepositoryPort clienteRepository;

    @Override
    public Cuenta crear(CrearCuentaCommand command) {
        clienteRepository.buscarPorId(command.clienteId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Cliente no encontrado con id " + command.clienteId()));

        String numero = generarNumeroCuenta(command.tipo());
        Cuenta cuenta = Cuenta.crear(command.tipo(), command.clienteId(),
                command.exentaGmf(), numero);

        Cuenta guardada = cuentaRepository.guardar(cuenta);
        log.info("Cuenta {} creada para el cliente {}", numero, command.clienteId());
        return guardada;
    }

    @Override
    public Cuenta activar(Long id) {
        return cuentaRepository.guardar(obtener(id).activar());
    }

    @Override
    public Cuenta inactivar(Long id) {
        return cuentaRepository.guardar(obtener(id).inactivar());
    }

    @Override
    public Cuenta cancelar(Long id) {
        return cuentaRepository.guardar(obtener(id).cancelar());
    }

    @Override
    @Transactional(readOnly = true)
    public Cuenta buscarPorId(Long id) {
        return obtener(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cuenta> listarPorClienteId(Long clienteId) {
        return cuentaRepository.listarPorClienteId(clienteId);
    }

    private Cuenta obtener(Long id) {
        return cuentaRepository.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Cuenta no encontrada con id " + id));
    }

    private String generarNumeroCuenta(TipoCuenta tipo) {
        String numero;
        do {
            numero = tipo.getPrefijo()
                    + String.format("%08d", ThreadLocalRandom.current().nextInt(100_000_000));
        } while (cuentaRepository.existePorNumeroCuenta(numero));
        return numero;
    }
}