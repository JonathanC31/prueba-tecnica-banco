package com.jonathan.banco.infrastructure.adapter.in.web;

import com.jonathan.banco.domain.model.Cliente;
import com.jonathan.banco.domain.model.TipoIdentificacion;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ClienteResponse(
        Long id, TipoIdentificacion tipoIdentificacion, String numeroIdentificacion,
        String nombres, String apellido, String correo, LocalDate fechaNacimiento,
        LocalDateTime fechaCreacion, LocalDateTime fechaModificacion) {

    public static ClienteResponse from(Cliente c) {
        return new ClienteResponse(c.getId(), c.getTipoIdentificacion(),
                c.getNumeroIdentificacion(), c.getNombres(), c.getApellido(),
                c.getCorreo(), c.getFechaNacimiento(),
                c.getFechaCreacion(), c.getFechaModificacion());
    }
}