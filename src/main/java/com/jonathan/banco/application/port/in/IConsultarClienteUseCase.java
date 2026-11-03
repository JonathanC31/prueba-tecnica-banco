package com.jonathan.banco.application.port.in;

import com.jonathan.banco.domain.model.Cliente;

import java.util.List;

public interface IConsultarClienteUseCase {
    Cliente buscarPorId(Long id);
    List<Cliente> listar();
}
