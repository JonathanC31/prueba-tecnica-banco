package com.jonathan.banco.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ITransaccionJpaRepository extends JpaRepository<TransaccionEntity, Long> {
    List<TransaccionEntity> findByCuentaOrigenIdOrCuentaDestinoId(Long origenId, Long destinoId);
}