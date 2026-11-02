package com.jonathan.banco.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface IClienteJpaRepository extends JpaRepository<ClienteEntity, Long> {
    boolean existsByNumeroIdentificacion(String numeroIdentificacion);
}