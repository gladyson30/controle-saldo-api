package com.example.controle_saldo_api.lancamento.dto;

import com.example.controle_saldo_api.lancamento.entity.Lancamento;
import com.example.controle_saldo_api.lancamento.entity.StatusLancamento;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record LancamentoResponseDto(
        UUID id,
        UUID contratoId,
        LocalDate data,
        BigDecimal valor,
        StatusLancamento status,
        LocalDateTime dataEstorno,
        String motivoEstorno
) {

    public static LancamentoResponseDto from(Lancamento lancamento) {
        return new LancamentoResponseDto(
                lancamento.getId(),
                lancamento.getContrato().getId(),
                lancamento.getData(),
                lancamento.getValor(),
                lancamento.getStatus(),
                lancamento.getDataEstorno(),
                lancamento.getMotivoEstorno()
        );
    }
}