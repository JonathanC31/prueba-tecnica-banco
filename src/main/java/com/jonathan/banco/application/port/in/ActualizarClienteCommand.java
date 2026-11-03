package com.jonathan.banco.application.port.in;

import java.time.LocalDate;

public record ActualizarClienteCommand(
        String nombres,
        String apellido,
        String correo,
        LocalDate fechaNacimiento) {

}
