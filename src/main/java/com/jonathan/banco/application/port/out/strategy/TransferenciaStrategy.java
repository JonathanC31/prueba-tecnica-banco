package com.jonathan.banco.application.port.out.strategy;

import com.jonathan.banco.application.port.in.EjecutarTransaccionCommand;
import com.jonathan.banco.application.port.out.ICuentaRepositoryPort;
import com.jonathan.banco.application.port.out.ITransaccionRepositoryPort;
import com.jonathan.banco.domain.exception.RecursoNoEncontradoException;
import com.jonathan.banco.domain.exception.ReglaNegocioException;
import com.jonathan.banco.domain.model.Cuenta;
import com.jonathan.banco.domain.model.TipoTransaccion;
import com.jonathan.banco.domain.model.Transaccion;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransferenciaStrategy implements ITransaccionStrategy {

    private final ICuentaRepositoryPort cuentaRepository;
    private final ITransaccionRepositoryPort transaccionRepository;

    @Override
    public TipoTransaccion tipo() { return TipoTransaccion.TRANSFERENCIA; }

    @Override
    public Transaccion ejecutar(EjecutarTransaccionCommand c) {
        if (c.cuentaOrigenId() == null) {
            throw new ReglaNegocioException("La cuenta origen es obligatoria");
        }
        if (c.cuentaDestinoId() == null) {
            throw new ReglaNegocioException("La cuenta destino es obligatoria");
        }
        if (c.cuentaOrigenId().equals(c.cuentaDestinoId())) {
            throw new ReglaNegocioException("La cuenta origen y destino deben ser distintas");
        }

        Cuenta origen = obtener(c.cuentaOrigenId());
        Cuenta destino = obtener(c.cuentaDestinoId());

        cuentaRepository.guardar(origen.retirar(c.monto()));
        cuentaRepository.guardar(destino.depositar(c.monto()));

        return transaccionRepository.guardar(Transaccion.crear(
                TipoTransaccion.TRANSFERENCIA, c.monto(),
                c.cuentaOrigenId(), c.cuentaDestinoId()));
    }

    private Cuenta obtener(Long id) {
        return cuentaRepository.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cuenta no encontrada con id " + id));
    }
}