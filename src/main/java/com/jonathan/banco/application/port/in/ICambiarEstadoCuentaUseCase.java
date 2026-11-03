package com.jonathan.banco.application.port.in;

import com.jonathan.banco.domain.model.Cuenta;

public interface ICambiarEstadoCuentaUseCase {
    Cuenta activar(Long id);
    Cuenta inactivar(Long id);
    Cuenta cancelar(Long id);
}