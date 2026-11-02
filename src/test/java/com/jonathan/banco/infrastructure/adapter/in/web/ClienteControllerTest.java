package com.jonathan.banco.infrastructure.adapter.in.web;

import com.jonathan.banco.application.port.in.ActualizarClienteCommand;
import com.jonathan.banco.application.port.in.CrearClienteCommand;
import com.jonathan.banco.application.port.in.IActualizarClienteUseCase;
import com.jonathan.banco.application.port.in.IConsultarClienteUseCase;
import com.jonathan.banco.application.port.in.ICrearClienteUseCase;
import com.jonathan.banco.application.port.in.IEliminarClienteUseCase;
import com.jonathan.banco.domain.exception.RecursoNoEncontradoException;
import com.jonathan.banco.domain.exception.ReglaNegocioException;
import com.jonathan.banco.domain.model.Cliente;
import com.jonathan.banco.domain.model.TipoIdentificacion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ClienteController.class)
class ClienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ICrearClienteUseCase crearCliente;

    @MockitoBean
    private IActualizarClienteUseCase actualizarCliente;

    @MockitoBean
    private IEliminarClienteUseCase eliminarCliente;

    @MockitoBean
    private IConsultarClienteUseCase consultarCliente;

    private Cliente clienteMuestra() {
        return Cliente.crear(
                TipoIdentificacion.CC, "1020304050", "Jonathan", "Cortes",
                "jonathan@banco.com", LocalDate.of(1998, 5, 15)
        ).toBuilder().id(1L).build();
    }

    @Test
    @DisplayName("POST /api/clientes retorna 201 cuando los datos son validos")
    void crearRetorna201() throws Exception {
        when(crearCliente.crear(any(CrearClienteCommand.class))).thenReturn(clienteMuestra());

        String json = """
                {
                  "tipoIdentificacion": "CC",
                  "numeroIdentificacion": "1020304050",
                  "nombres": "Jonathan",
                  "apellido": "Cortes",
                  "correo": "jonathan@banco.com",
                  "fechaNacimiento": "1998-05-15"
                }
                """;

        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombres").value("Jonathan"))
                .andExpect(jsonPath("$.numeroIdentificacion").value("1020304050"));
    }

    @Test
    @DisplayName("POST /api/clientes retorna 400 cuando falla Bean Validation")
    void crearInvalidoRetorna400() throws Exception {
        String json = """
                {
                  "tipoIdentificacion": "CC",
                  "numeroIdentificacion": "",
                  "nombres": "J",
                  "apellido": "",
                  "correo": "correo-invalido",
                  "fechaNacimiento": "1998-05-15"
                }
                """;

        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Error de validación"))
                .andExpect(jsonPath("$.errores.nombres").exists())
                .andExpect(jsonPath("$.errores.correo").exists());
    }

    @Test
    @DisplayName("GET /api/clientes/{id} retorna 200 cuando existe y 404 cuando no existe")
    void buscarPorIdRetorna200Y404() throws Exception {
        when(consultarCliente.buscarPorId(1L)).thenReturn(clienteMuestra());
        when(consultarCliente.buscarPorId(99L))
                .thenThrow(new RecursoNoEncontradoException("No existe un cliente con id 99"));

        mockMvc.perform(get("/api/clientes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        mockMvc.perform(get("/api/clientes/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("No existe un cliente con id 99"));
    }

    @Test
    @DisplayName("GET /api/clientes retorna 200 con la lista de clientes")
    void listarRetorna200() throws Exception {
        when(consultarCliente.listar()).thenReturn(List.of(clienteMuestra()));

        mockMvc.perform(get("/api/clientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    @DisplayName("PUT /api/clientes/{id} retorna 200 con datos actualizados")
    void actualizarRetorna200() throws Exception {
        Cliente actualizado = clienteMuestra().actualizar(
                "Jonathan Andres", "Cortes", "nuevo@banco.com", LocalDate.of(1998, 5, 15)
        );
        when(actualizarCliente.actualizar(eq(1L), any(ActualizarClienteCommand.class)))
                .thenReturn(actualizado);

        String json = """
                {
                  "nombres": "Jonathan Andres",
                  "apellido": "Cortes",
                  "correo": "nuevo@banco.com",
                  "fechaNacimiento": "1998-05-15"
                }
                """;

        mockMvc.perform(put("/api/clientes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombres").value("Jonathan Andres"))
                .andExpect(jsonPath("$.correo").value("nuevo@banco.com"));
    }

    @Test
    @DisplayName("DELETE /api/clientes/{id} retorna 204 al eliminar y 422 si tiene cuentas")
    void eliminarRetorna204Y422() throws Exception {
        mockMvc.perform(delete("/api/clientes/1"))
                .andExpect(status().isNoContent());
        verify(eliminarCliente).eliminar(1L);

        doThrow(new ReglaNegocioException("No se puede eliminar un cliente con productos vinculados"))
                .when(eliminarCliente).eliminar(2L);

        mockMvc.perform(delete("/api/clientes/2"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.mensaje").value("No se puede eliminar un cliente con productos vinculados"));
    }
}
