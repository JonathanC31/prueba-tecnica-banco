package com.jonathan.banco.application.port.in;

import com.jonathan.banco.domain.model.Transaccion;

public interface IRegistrarTransaccionUseCase {
    Transaccion ejecutar(EjecutarTransaccionCommand command);
}