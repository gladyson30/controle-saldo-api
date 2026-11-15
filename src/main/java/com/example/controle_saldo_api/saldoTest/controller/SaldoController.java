package com.example.controle_saldo_api.saldoTest.controller;

import com.example.controle_saldo_api.saldoTest.dto.SaldoConsolidadoResponseDto;
import com.example.controle_saldo_api.saldoTest.dto.SaldoResponseDto;
import com.example.controle_saldo_api.saldoTest.service.SaldoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/saldo")
@RequiredArgsConstructor
public class SaldoController {

    private final SaldoService saldoService;

    @GetMapping
    public SaldoConsolidadoResponseDto saldoConsolidado() {
        return saldoService.saldoConsolidado();
    }

    @GetMapping("/contrato/{contratoId}")
    public SaldoResponseDto saldoDoContrato(@PathVariable UUID contratoId) {
        return saldoService.saldoDoContrato(contratoId);
    }
}