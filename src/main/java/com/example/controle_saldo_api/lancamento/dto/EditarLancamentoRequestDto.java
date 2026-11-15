package com.example.controle_saldo_api.lancamento.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EditarLancamentoRequestDto(
        @NotNull
        LocalDate data,

        @NotNull
        @DecimalMin(value = "0.01", message = "valor deve ser maior que zero")
        BigDecimal valor
) {
}
