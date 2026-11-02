package com.jonathan.banco.infrastructure.adapter.out.persistence;

import com.jonathan.banco.application.port.out.ITransaccionRepositoryPort;
import com.jonathan.banco.domain.model.Transaccion;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TransaccionPersistenceAdapter implements ITransaccionRepositoryPort {

    private final ITransaccionJpaRepository jpaRepository;
    private final TransaccionMapper mapper;

    @Override
    public Transaccion guardar(Transaccion t) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(t)));
    }

    @Override
    public List<Transaccion> listarPorCuentaId(Long cuentaId) {
        return jpaRepository.findByCuentaOrigenIdOrCuentaDestinoId(cuentaId, cuentaId)
                .stream().map(mapper::toDomain).toList();
    }
}