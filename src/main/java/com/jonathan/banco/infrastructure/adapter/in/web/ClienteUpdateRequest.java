package com.jonathan.banco.infrastructure.adapter.in.web;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record ClienteUpdateRequest(
        @NotBlank @Size(min = 2) String nombres,
        @NotBlank @Size(min = 2) String apellido,
        @NotBlank @Email String correo,
        @NotNull @Past LocalDate fechaNacimiento) {}