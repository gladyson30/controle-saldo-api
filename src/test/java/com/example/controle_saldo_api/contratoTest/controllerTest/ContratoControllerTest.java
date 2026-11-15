package com.example.controle_saldo_api.contratoTest.controllerTest;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@AutoConfigureMockMvc
@SpringBootTest
@ActiveProfiles("test")
class ContratoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void criarRetorna201ComDataFimCalculada() throws Exception {
        mockMvc.perform(post("/api/contratos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Contrato Teste","valorContratado":100000.00,"dataInicio":"2026-01-01"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.dataFim").value("2027-01-01"));
    }

    @Test
    void criarComValorZeroRetorna400() throws Exception {
        mockMvc.perform(post("/api/contratos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Contrato","valorContratado":0,"dataInicio":"2026-01-01"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listarRetorna200() throws Exception {
        mockMvc.perform(get("/api/contratos"))
                .andExpect(status().isOk());
    }

    @Test
    void buscarIdInexistenteRetorna404() throws Exception {
        mockMvc.perform(get("/api/contratos/00000000-0000-0000-0000-000000000000"))
                .andExpect(status().isNotFound());
    }
}