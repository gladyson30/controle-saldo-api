package com.example.controle_saldo_api.lancamentoTest.controllerTes;


import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LancamentoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void registrarRetorna201ComStatusAtivo() throws Exception {
        String contratoId = criarContrato();

        mockMvc.perform(post("/lancamento/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"contratoId":"%s","data":"2026-03-01","valor":15000.00}
                                """.formatted(contratoId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ATIVO"));
    }

    @Test
    void registrarValorAcimaDoSaldoRetorna422() throws Exception {
        String contratoId = criarContrato();

        mockMvc.perform(post("/lancamento/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"contratoId":"%s","data":"2026-03-01","valor":100000.01}
                                """.formatted(contratoId)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void listarPorContratoRetorna200() throws Exception {
        String contratoId = criarContrato();
        registrarLancamento(contratoId, "15000.00");

        mockMvc.perform(get("/lancamento/listar/" + contratoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].valor").value(15000.00));
    }

    @Test
    void editarRetorna200ComNovoValor() throws Exception {
        String contratoId = criarContrato();
        String lancamentoId = registrarLancamento(contratoId, "15000.00");

        mockMvc.perform(put("/lancamento/editar/" + lancamentoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"data":"2026-03-10","valor":20000.00}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valor").value(20000.00));
    }

    @Test
    void estornarRetorna200ComStatusEstornado() throws Exception {
        String contratoId = criarContrato();
        String lancamentoId = registrarLancamento(contratoId, "15000.00");

        mockMvc.perform(post("/lancamento/estorno/" + lancamentoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ESTORNADO"));
    }

    @Test
    void estornarDuasVezesRetorna409() throws Exception {
        String contratoId = criarContrato();
        String lancamentoId = registrarLancamento(contratoId, "15000.00");
        mockMvc.perform(post("/lancamento/estorno/" + lancamentoId));

        mockMvc.perform(post("/lancamento/estorno/" + lancamentoId))
                .andExpect(status().isConflict());
    }

    private String criarContrato() throws Exception {
        String resposta = mockMvc.perform(post("/api/contratos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Contrato Lançamento","valorContratado":100000.00,"dataInicio":"2026-01-01"}
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(resposta, "$.id");
    }

    private String registrarLancamento(String contratoId, String valor) throws Exception {
        String resposta = mockMvc.perform(post("/lancamento/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"contratoId":"%s","data":"2026-03-01","valor":%s}
                                """.formatted(contratoId, valor)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(resposta, "$.id");
    }
}