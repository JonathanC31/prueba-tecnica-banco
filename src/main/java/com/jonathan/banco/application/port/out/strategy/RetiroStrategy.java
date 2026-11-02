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
public class RetiroStrategy implements ITransaccionStrategy {

    private final ICuentaRepositoryPort cuentaRepository;
    private final ITransaccionRepositoryPort transaccionRepository;

    @Override
    public TipoTransaccion tipo() { return TipoTransaccion.RETIRO; }

    @Override
    public Transaccion ejecutar(EjecutarTransaccionCommand c) {
        if (c.cuentaOrigenId() == null) {
            throw new ReglaNegocioException("La cuenta origen es obligatoria");
        }
        Cuenta origen = cuentaRepository.buscarPorId(c.cuentaOrigenId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Cuenta no encontrada con id " + c.cuentaOrigenId()));

        cuentaRepository.guardar(origen.retirar(c.monto()));

        return transaccionRepository.guardar(Transaccion.crear(
                TipoTransaccion.RETIRO, c.monto(), c.cuentaOrigenId(), null));
    }
}