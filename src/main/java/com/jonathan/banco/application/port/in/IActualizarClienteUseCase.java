package com.jonathan.banco.application.port.in;

import com.jonathan.banco.domain.model.Cliente;

public interface IActualizarClienteUseCase {
    Cliente actualizar(Long id, ActualizarClienteCommand command);
}
