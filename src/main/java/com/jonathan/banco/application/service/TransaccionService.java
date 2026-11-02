package com.jonathan.banco.application.service;

import com.jonathan.banco.application.port.in.EjecutarTransaccionCommand;
import com.jonathan.banco.application.port.in.IConsultarTransaccionUseCase;
import com.jonathan.banco.application.port.in.IRegistrarTransaccionUseCase;
import com.jonathan.banco.application.port.out.strategy.ITransaccionStrategy;
import com.jonathan.banco.application.port.out.ITransaccionRepositoryPort;
import com.jonathan.banco.domain.model.TipoTransaccion;
import com.jonathan.banco.domain.model.Transaccion;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class TransaccionService implements IRegistrarTransaccionUseCase, IConsultarTransaccionUseCase {

    private final Map<TipoTransaccion, ITransaccionStrategy> estrategias;
    private final ITransaccionRepositoryPort transaccionRepository;

    public TransaccionService(List<ITransaccionStrategy> lista,
                              ITransaccionRepositoryPort transaccionRepository) {
        this.estrategias = lista.stream()
                .collect(Collectors.toMap(ITransaccionStrategy::tipo, Function.identity()));
        this.transaccionRepository = transaccionRepository;
    }

    @Override
    public Transaccion ejecutar(EjecutarTransaccionCommand command) {
        return estrategias.get(command.tipo()).ejecutar(command);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Transaccion> listarPorCuentaId(Long cuentaId) {
        return transaccionRepository.listarPorCuentaId(cuentaId);
    }
}