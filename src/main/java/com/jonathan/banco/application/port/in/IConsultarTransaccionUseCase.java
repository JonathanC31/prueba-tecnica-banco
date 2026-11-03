package com.jonathan.banco.application.port.in;

import com.jonathan.banco.domain.model.Transaccion;

import java.util.List;

public interface IConsultarTransaccionUseCase {
    List<Transaccion> listarPorCuentaId(Long cuentaId);
}