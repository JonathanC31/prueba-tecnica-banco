package com.jonathan.banco.infrastructure.adapter.in.web;

import com.jonathan.banco.application.port.in.EjecutarTransaccionCommand;
import com.jonathan.banco.application.port.in.IConsultarTransaccionUseCase;
import com.jonathan.banco.application.port.in.IRegistrarTransaccionUseCase;
import com.jonathan.banco.domain.model.TipoTransaccion;
import com.jonathan.banco.domain.model.Transaccion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransaccionController.class)
class TransaccionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IRegistrarTransaccionUseCase registrar;

    @MockitoBean
    private IConsultarTransaccionUseCase consultar;

    @Test
    @DisplayName("POST /api/transacciones retorna 201 al registrar una transaccion valida")
    void ejecutarRetorna201() throws Exception {
        Transaccion creada = Transaccion.crear(
                TipoTransaccion.TRANSFERENCIA, new BigDecimal("120000"), 10L, 20L
        ).toBuilder().id(100L).build();
        when(registrar.ejecutar(any(EjecutarTransaccionCommand.class))).thenReturn(creada);

        String json = """
                {
                  "tipo": "TRANSFERENCIA",
                  "monto": 120000,
                  "cuentaOrigenId": 10,
                  "cuentaDestinoId": 20
                }
                """;

        mockMvc.perform(post("/api/transacciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.tipo").value("TRANSFERENCIA"))
                .andExpect(jsonPath("$.monto").value(120000));
    }

    @Test
    @DisplayName("POST /api/transacciones retorna 400 cuando el monto es negativo o cero")
    void ejecutarMontoInvalidoRetorna400() throws Exception {
        String json = """
                {
                  "tipo": "CONSIGNACION",
                  "monto": -500,
                  "cuentaDestinoId": 10
                }
                """;

        mockMvc.perform(post("/api/transacciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.monto").exists());
    }

    @Test
    @DisplayName("GET /api/transacciones?cuentaId=10 retorna 200 con el historial de la cuenta")
    void listarPorCuentaIdRetorna200() throws Exception {
        Transaccion t = Transaccion.crear(
                TipoTransaccion.CONSIGNACION, new BigDecimal("50000"), null, 10L
        ).toBuilder().id(100L).build();
        when(consultar.listarPorCuentaId(10L)).thenReturn(List.of(t));

        mockMvc.perform(get("/api/transacciones").param("cuentaId", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(100))
                .andExpect(jsonPath("$[0].tipo").value("CONSIGNACION"));
    }
}
