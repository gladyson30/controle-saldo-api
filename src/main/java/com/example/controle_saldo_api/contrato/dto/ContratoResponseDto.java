package com.example.controle_saldo_api.contrato.dto;
import com.example.controle_saldo_api.contrato.entity.Contrato;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;


public record ContratoResponseDto(
        UUID id,
        String nome,
        BigDecimal valorContratado,
        LocalDate dataInicio,
        LocalDate dataFim
) {

    public static ContratoResponseDto from(Contrato contrato) {
        return new ContratoResponseDto(
                contrato.getId(),
                contrato.getNome(),
                contrato.getValorContratado(),
                contrato.getDataInicio(),
                contrato.getDataFim()
        );
    }
}