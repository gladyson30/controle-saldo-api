package com.example.controle_saldo_api.saldoTest.dto;

import java.math.BigDecimal;
import java.util.List;

public record SaldoConsolidadoResponseDto(
        BigDecimal valorTotalContratado,
        BigDecimal totalLancado,
        BigDecimal saldoDisponivel,
        List<SaldoResponseDto> contratos
) {
}
