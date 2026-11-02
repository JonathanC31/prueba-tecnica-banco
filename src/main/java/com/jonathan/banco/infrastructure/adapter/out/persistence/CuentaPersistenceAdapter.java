package com.jonathan.banco.infrastructure.adapter.out.persistence;

import com.jonathan.banco.application.port.out.ICuentaRepositoryPort;
import com.jonathan.banco.domain.model.Cuenta;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CuentaPersistenceAdapter implements ICuentaRepositoryPort {

    private final ICuentaJpaRepository jpaRepository;
    private final CuentaMapper mapper;

    @Override
    public Cuenta guardar(Cuenta cuenta) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(cuenta)));
    }

    @Override
    public Optional<Cuenta> buscarPorId(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public boolean existePorNumeroCuenta(String numeroCuenta) {
        return jpaRepository.existsByNumeroCuenta(numeroCuenta);
    }

    @Override
    public List<Cuenta> listarPorClienteId(Long clienteId) {
        return jpaRepository.findByClienteId(clienteId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public boolean existePorClienteId(Long clienteId) {
        return jpaRepository.existsByClienteId(clienteId);
    }
}