package com.jonathan.banco.application.port.in;

import com.jonathan.banco.domain.model.TipoIdentificacion;
import java.time.LocalDate;

public record CrearClienteCommand(
        TipoIdentificacion tipoIdentificacion,
        String numeroIdentificacion,
        String nombres,
        String apellido,
        String correo,
        LocalDate fechaNacimiento) {}