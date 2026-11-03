package com.jonathan.banco.application.port.out.strategy;

import com.jonathan.banco.application.port.in.EjecutarTransaccionCommand;
import com.jonathan.banco.domain.model.TipoTransaccion;
import com.jonathan.banco.domain.model.Transaccion;

public interface ITransaccionStrategy {
    TipoTransaccion tipo();
    Transaccion ejecutar(EjecutarTransaccionCommand command);
}