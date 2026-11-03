package com.jonathan.banco.infrastructure.adapter.out.persistence;

import com.jonathan.banco.domain.model.Cliente;
import org.springframework.stereotype.Component;

@Component
public class ClienteMapper {

    public ClienteEntity toEntity(Cliente c) {
        ClienteEntity e = new ClienteEntity();
        e.setId(c.getId());
        e.setTipoIdentificacion(c.getTipoIdentificacion());
        e.setNumeroIdentificacion(c.getNumeroIdentificacion());
        e.setNombres(c.getNombres());
        e.setApellido(c.getApellido());
        e.setCorreo(c.getCorreo());
        e.setFechaNacimiento(c.getFechaNacimiento());
        e.setFechaCreacion(c.getFechaCreacion());
        e.setFechaModificacion(c.getFechaModificacion());
        return e;
    }

    public Cliente toDomain(ClienteEntity e) {
        return Cliente.builder()
                .id(e.getId())
                .tipoIdentificacion(e.getTipoIdentificacion())
                .numeroIdentificacion(e.getNumeroIdentificacion())
                .nombres(e.getNombres())
                .apellido(e.getApellido())
                .correo(e.getCorreo())
                .fechaNacimiento(e.getFechaNacimiento())
                .fechaCreacion(e.getFechaCreacion())
                .fechaModificacion(e.getFechaModificacion())
                .build();
    }
}