package com.jonathan.banco.infrastructure.adapter.out.persistence;

import com.jonathan.banco.application.port.out.IClienteRepositoryPort;
import com.jonathan.banco.domain.model.Cliente;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ClientePersistenceAdapter implements IClienteRepositoryPort {

    private final IClienteJpaRepository jpaRepository;
    private final ClienteMapper mapper;

    @Override
    public Cliente guardar(Cliente cliente) {
        ClienteEntity guardada = jpaRepository.save(mapper.toEntity(cliente));
        return mapper.toDomain(guardada);
    }

    @Override
    public Optional<Cliente> buscarPorId(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Cliente> listar() {
        return jpaRepository.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public boolean existePorNumeroIdentificacion(String numero) {
        return jpaRepository.existsByNumeroIdentificacion(numero);
    }

    @Override
    public void eliminar(Long id) {
        jpaRepository.deleteById(id);
    }
}