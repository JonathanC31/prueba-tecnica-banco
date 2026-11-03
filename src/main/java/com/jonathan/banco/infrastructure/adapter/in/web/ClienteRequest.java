package com.jonathan.banco.infrastructure.adapter.in.web;

import com.jonathan.banco.domain.model.TipoIdentificacion;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record ClienteRequest(
        @NotNull(message = "El tipo de identificación es obligatorio")
        TipoIdentificacion tipoIdentificacion,

        @NotBlank(message = "El número de identificación es obligatorio")
        String numeroIdentificacion,

        @NotBlank(message = "Los nombres son obligatorios")
        @Size(min = 2, message = "Los nombres deben tener al menos 2 caracteres")
        String nombres,

        @NotBlank(message = "El apellido es obligatorio")
        @Size(min = 2, message = "El apellido debe tener al menos 2 caracteres")
        String apellido,

        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no tiene un formato válido")
        String correo,

        @NotNull(message = "La fecha de nacimiento es obligatoria")
        @Past(message = "La fecha de nacimiento debe ser pasada")
        LocalDate fechaNacimiento) {}
