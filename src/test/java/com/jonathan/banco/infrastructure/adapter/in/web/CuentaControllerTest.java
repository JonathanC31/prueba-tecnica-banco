package com.jonathan.banco.infrastructure.adapter.in.web;

import com.jonathan.banco.application.port.in.CrearCuentaCommand;
import com.jonathan.banco.application.port.in.ICambiarEstadoCuentaUseCase;
import com.jonathan.banco.application.port.in.IConsultarCuentaUseCase;
import com.jonathan.banco.application.port.in.ICrearCuentaUseCase;
import com.jonathan.banco.domain.exception.ReglaNegocioException;
import com.jonathan.banco.domain.model.Cuenta;
import com.jonathan.banco.domain.model.EstadoCuenta;
import com.jonathan.banco.domain.model.TipoCuenta;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CuentaController.class)
class CuentaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ICrearCuentaUseCase crearCuenta;

    @MockitoBean
    private ICambiarEstadoCuentaUseCase cambiarEstado;

    @MockitoBean
    private IConsultarCuentaUseCase consultarCuenta;

    private Cuenta cuentaMuestra() {
        return Cuenta.crear(TipoCuenta.AHORROS, 1L, true, "5300000001")
                .toBuilder().id(10L).build();
    }

    @Test
    @DisplayName("POST /api/cuentas retorna 201 cuando se crea la cuenta")
    void crearRetorna201() throws Exception {
        when(crearCuenta.crear(any(CrearCuentaCommand.class))).thenReturn(cuentaMuestra());

        String json = """
                {
                  "tipo": "AHORROS",
                  "clienteId": 1,
                  "exentaGmf": true
                }
                """;

        mockMvc.perform(post("/api/cuentas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.numeroCuenta").value("5300000001"))
                .andExpect(jsonPath("$.estado").value("ACTIVA"));
    }

    @Test
    @DisplayName("GET /api/cuentas/{id} y GET /api/cuentas?clienteId=1 retornan 200")
    void buscarYListarRetornan200() throws Exception {
        when(consultarCuenta.buscarPorId(10L)).thenReturn(cuentaMuestra());
        when(consultarCuenta.listarPorClienteId(1L)).thenReturn(List.of(cuentaMuestra()));

        mockMvc.perform(get("/api/cuentas/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numeroCuenta").value("5300000001"));

        mockMvc.perform(get("/api/cuentas").param("clienteId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10));
    }

    @Test
    @DisplayName("PATCH activar, inactivar y cancelar cambian el estado de la cuenta")
    void cambiarEstadoRetorna200Y422() throws Exception {
        Cuenta inactiva = cuentaMuestra().inactivar();
        Cuenta activa = inactiva.activar();
        Cuenta cancelada = activa.cancelar();

        when(cambiarEstado.inactivar(10L)).thenReturn(inactiva);
        when(cambiarEstado.activar(10L)).thenReturn(activa);
        when(cambiarEstado.cancelar(10L)).thenReturn(cancelada);
        when(cambiarEstado.cancelar(20L))
                .thenThrow(new ReglaNegocioException("Solo se puede cancelar una cuenta con saldo 0"));

        mockMvc.perform(patch("/api/cuentas/10/inactivar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value(EstadoCuenta.INACTIVA.name()));

        mockMvc.perform(patch("/api/cuentas/10/activar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value(EstadoCuenta.ACTIVA.name()));

        mockMvc.perform(patch("/api/cuentas/10/cancelar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value(EstadoCuenta.CANCELADA.name()));

        mockMvc.perform(patch("/api/cuentas/20/cancelar"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.mensaje").value("Solo se puede cancelar una cuenta con saldo 0"));
    }

    @Test
    @DisplayName("GlobalExceptionHandler maneja conflictos 409 y errores inesperados 500")
    void manejoDeConflictosYErrores500() throws Exception {
        when(cambiarEstado.activar(30L))
                .thenThrow(new DataIntegrityViolationException("duplicate key"));
        when(cambiarEstado.activar(31L))
                .thenThrow(new ObjectOptimisticLockingFailureException("CuentaEntity", 31L));
        when(cambiarEstado.activar(32L))
                .thenThrow(new RuntimeException("Fallo inesperado"));

        mockMvc.perform(patch("/api/cuentas/30/activar"))
                .andExpect(status().isConflict());

        mockMvc.perform(patch("/api/cuentas/31/activar"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value("La cuenta fue modificada por otra operación. Intente nuevamente."));

        mockMvc.perform(patch("/api/cuentas/32/activar"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.mensaje").value("Ocurrió un error inesperado. Intente más tarde."));
    }
}
