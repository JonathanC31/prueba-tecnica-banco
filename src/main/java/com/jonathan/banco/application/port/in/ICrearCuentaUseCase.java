package com.jonathan.banco.application.port.in;

import com.jonathan.banco.domain.model.Cuenta;

import java.util.List;

public interface ICrearCuentaUseCase {
    Cuenta crear(CrearCuentaCommand command);
}

