package com.example.controle_saldo_api.contrato.dto;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ContratoDto(
        @NotBlank
        @Size(max = 150)
        String nome,

        @NotNull
        @DecimalMin(value = "0.01", message = "valor contratado deve ser maior que zero")
        BigDecimal valorContratado,

        @NotNull
        LocalDate dataInicio
) {
}
