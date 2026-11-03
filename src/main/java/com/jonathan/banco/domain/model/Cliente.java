package com.jonathan.banco.domain.model;

import com.jonathan.banco.domain.exception.ReglaNegocioException;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

@Getter
@Builder(toBuilder = true)
public class Cliente {

    private final Long id;
    private final TipoIdentificacion tipoIdentificacion;
    private final String numeroIdentificacion;
    private final String nombres;
    private final String apellido;
    private final String correo;
    private final LocalDate fechaNacimiento;
    private final LocalDateTime fechaCreacion;
    private final LocalDateTime fechaModificacion;

    /** Crea un cliente nuevo aplicando las reglas de negocio. */
    public static Cliente crear(TipoIdentificacion tipo, String numero, String nombres,
                                String apellido, String correo, LocalDate fechaNacimiento) {
        validarMayorDeEdad(fechaNacimiento);
        return Cliente.builder()
                .tipoIdentificacion(tipo)
                .numeroIdentificacion(numero)
                .nombres(nombres)
                .apellido(apellido)
                .correo(correo)
                .fechaNacimiento(fechaNacimiento)
                .fechaCreacion(LocalDateTime.now())   // automática
                .build();
    }

    /** Devuelve una copia con los datos nuevos y la fecha de modificación actualizada. */
    public Cliente actualizar(String nombres, String apellido, String correo,
                              LocalDate fechaNacimiento) {
        validarMayorDeEdad(fechaNacimiento);
        return this.toBuilder()
                .nombres(nombres)
                .apellido(apellido)
                .correo(correo)
                .fechaNacimiento(fechaNacimiento)
                .fechaModificacion(LocalDateTime.now())   // automática
                .build();
    }

    private static void validarMayorDeEdad(LocalDate fechaNacimiento) {
        if (fechaNacimiento == null
                || Period.between(fechaNacimiento, LocalDate.now()).getYears() < 18) {
            throw new ReglaNegocioException("El cliente debe ser mayor de edad");
        }
    }
}