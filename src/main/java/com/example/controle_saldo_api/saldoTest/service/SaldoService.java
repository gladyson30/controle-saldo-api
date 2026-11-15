package com.example.controle_saldo_api.saldoTest.service;

import com.example.controle_saldo_api.contrato.entity.Contrato;
import com.example.controle_saldo_api.contrato.service.ContratoService;
import com.example.controle_saldo_api.lancamento.entity.StatusLancamento;
import com.example.controle_saldo_api.lancamento.repository.LancamentoRepository;
import com.example.controle_saldo_api.saldoTest.dto.AlertaSaldo;
import com.example.controle_saldo_api.saldoTest.dto.SaldoConsolidadoResponseDto;
import com.example.controle_saldo_api.saldoTest.dto.SaldoResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SaldoService {

    static final BigDecimal LIMITE_PROXIMIDADE = new BigDecimal("80");
    static final BigDecimal LIMITE_SUBUTILIZACAO = new BigDecimal("20");
    static final BigDecimal VIGENCIA_MINIMA_SUBUTILIZACAO = new BigDecimal("50");

    private static final BigDecimal CEM = new BigDecimal("100");

    private final ContratoService contratoService;
    private final LancamentoRepository lancamentoRepository;
    private final Clock clock;

    public SaldoResponseDto saldoDoContrato(UUID contratoId) {
        Contrato contrato = contratoService.buscarEntidade(contratoId);
        return montarSaldo(contrato, LocalDate.now(clock));
    }

    public SaldoConsolidadoResponseDto saldoConsolidado() {
        LocalDate hoje = LocalDate.now(clock);
        List<SaldoResponseDto> contratos = contratoService.listarEntidades().stream()
                .map(contrato -> montarSaldo(contrato, hoje))
                .toList();

        BigDecimal totalContratado = contratos.stream()
                .map(SaldoResponseDto::valorContratado)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalLancado = contratos.stream()
                .map(SaldoResponseDto::totalLancado)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new SaldoConsolidadoResponseDto(
                totalContratado,
                totalLancado,
                totalContratado.subtract(totalLancado),
                contratos
        );
    }

    private SaldoResponseDto montarSaldo(Contrato contrato, LocalDate hoje) {
        BigDecimal totalLancado = lancamentoRepository.somarPorContratoEStatus(
                contrato.getId(), StatusLancamento.ATIVO);
        BigDecimal saldo = contrato.getValorContratado().subtract(totalLancado);

        BigDecimal percentualConsumido = percentual(totalLancado, contrato.getValorContratado());
        BigDecimal percentualVigencia = percentualVigenciaDecorrida(contrato, hoje);

        return new SaldoResponseDto(
                contrato.getId(),
                contrato.getNome(),
                contrato.getValorContratado(),
                totalLancado,
                saldo,
                percentualConsumido,
                percentualVigencia,
                calcularAlerta(percentualConsumido, percentualVigencia)
        );
    }

    private AlertaSaldo calcularAlerta(BigDecimal percentualConsumido, BigDecimal percentualVigencia) {
        if (percentualConsumido.compareTo(LIMITE_PROXIMIDADE) >= 0) {
            return AlertaSaldo.PROXIMO_DO_LIMITE;
        }
        if (percentualConsumido.compareTo(LIMITE_SUBUTILIZACAO) < 0
                && percentualVigencia.compareTo(VIGENCIA_MINIMA_SUBUTILIZACAO) >= 0) {
            return AlertaSaldo.RISCO_SUBUTILIZACAO;
        }
        return AlertaSaldo.NENHUM;
    }

    private BigDecimal percentual(BigDecimal parte, BigDecimal total) {
        if (total.signum() == 0) {
            return BigDecimal.ZERO;
        }
        return parte.multiply(CEM).divide(total, 2, RoundingMode.HALF_UP);
    }

    private BigDecimal percentualVigenciaDecorrida(Contrato contrato, LocalDate hoje) {
        long diasTotais = ChronoUnit.DAYS.between(contrato.getDataInicio(), contrato.getDataFim());
        if (diasTotais <= 0) {
            return BigDecimal.ZERO;
        }
        long diasDecorridos = ChronoUnit.DAYS.between(contrato.getDataInicio(), hoje);
        long limitados = Math.max(0, Math.min(diasDecorridos, diasTotais));
        return percentual(BigDecimal.valueOf(limitados), BigDecimal.valueOf(diasTotais));
    }
}