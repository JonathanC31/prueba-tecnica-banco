package com.jonathan.banco.application.port.in;

import com.jonathan.banco.domain.model.Cuenta;

import java.util.List;

public interface IConsultarCuentaUseCase {
    Cuenta buscarPorId(Long id);
    List<Cuenta> listarPorClienteId(Long clienteId);
}