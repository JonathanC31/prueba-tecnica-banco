package com.jonathan.banco.application.port.out;

import com.jonathan.banco.domain.model.Transaccion;

import java.util.List;

public interface ITransaccionRepositoryPort {
    Transaccion guardar(Transaccion transaccion);
    List<Transaccion> listarPorCuentaId(Long cuentaId);
}