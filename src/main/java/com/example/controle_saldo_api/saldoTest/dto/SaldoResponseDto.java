package com.example.controle_saldo_api.saldoTest.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record SaldoResponseDto(
        UUID contratoId,
        String nome,
        BigDecimal valorContratado,
        BigDecimal totalLancado,
        BigDecimal saldoDisponivel,
        BigDecimal percentualConsumido,
        BigDecimal percentualVigenciaDecorrida,
        AlertaSaldo alerta
) {
}
