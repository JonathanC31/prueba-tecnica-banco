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
public class ConsignacionStrategy implements ITransaccionStrategy {

    private final ICuentaRepositoryPort cuentaRepository;
    private final ITransaccionRepositoryPort transaccionRepository;

    @Override
    public TipoTransaccion tipo() { return TipoTransaccion.CONSIGNACION; }

    @Override
    public Transaccion ejecutar(EjecutarTransaccionCommand c) {
        if (c.cuentaDestinoId() == null) {
            throw new ReglaNegocioException("La cuenta destino es obligatoria");
        }
        Cuenta destino = cuentaRepository.buscarPorId(c.cuentaDestinoId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Cuenta no encontrada con id " + c.cuentaDestinoId()));

        cuentaRepository.guardar(destino.depositar(c.monto()));

        return transaccionRepository.guardar(Transaccion.crear(
                TipoTransaccion.CONSIGNACION, c.monto(), null, c.cuentaDestinoId()));
    }
}