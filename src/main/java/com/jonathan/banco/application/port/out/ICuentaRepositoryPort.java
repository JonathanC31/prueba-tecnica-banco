package com.jonathan.banco.application.port.out;

import com.jonathan.banco.domain.model.Cuenta;

import java.util.List;
import java.util.Optional;

public interface ICuentaRepositoryPort {
    Cuenta guardar(Cuenta cuenta);
    Optional<Cuenta> buscarPorId(Long id);
    boolean existePorNumeroCuenta(String numeroCuenta);
    List<Cuenta> listarPorClienteId(Long clienteId);
    boolean existePorClienteId(Long clienteId);
}