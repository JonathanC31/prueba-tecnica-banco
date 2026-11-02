package com.jonathan.banco.application.port.in;
import com.jonathan.banco.domain.model.Cliente;

public interface ICrearClienteUseCase {
    Cliente crear(CrearClienteCommand command);
}
