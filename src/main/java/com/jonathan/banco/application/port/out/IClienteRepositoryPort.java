package com.jonathan.banco.application.port.out;

import com.jonathan.banco.domain.model.Cliente;

import java.util.List;
import java.util.Optional;

public interface IClienteRepositoryPort {
    Cliente guardar(Cliente cliente);
    Optional<Cliente> buscarPorId(Long id);
    List<Cliente> listar();
    boolean existePorNumeroIdentificacion(String numero);
    void eliminar(Long id);
}
