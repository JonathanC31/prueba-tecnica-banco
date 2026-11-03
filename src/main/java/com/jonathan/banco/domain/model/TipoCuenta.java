package com.jonathan.banco.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TipoCuenta {
    AHORROS("53"),
    CORRIENTE("33");

    private final String prefijo;
}