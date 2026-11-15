package com.example.controle_saldo_api.saldoTest.serviceTest;

import com.example.controle_saldo_api.contrato.entity.Contrato;
import com.example.controle_saldo_api.contrato.service.ContratoService;
import com.example.controle_saldo_api.lancamento.entity.StatusLancamento;
import com.example.controle_saldo_api.lancamento.repository.LancamentoRepository;
import com.example.controle_saldo_api.saldoTest.dto.AlertaSaldo;
import com.example.controle_saldo_api.saldoTest.dto.SaldoConsolidadoResponseDto;
import com.example.controle_saldo_api.saldoTest.dto.SaldoResponseDto;
import com.example.controle_saldo_api.saldoTest.service.SaldoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;



@ExtendWith(MockitoExtension.class)
class SaldoServiceTest {

    @Mock
    private ContratoService contratoService;

    @Mock
    private LancamentoRepository lancamentoRepository;

    private final UUID contratoId = UUID.randomUUID();

    // hoje fixo em 2026-11-15, para os cálculos de vigência serem previsíveis
    private final Clock clock = Clock.fixed(
            Instant.parse("2026-11-15T12:00:00Z"), ZoneOffset.UTC);

    private SaldoService saldoService;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        saldoService = new SaldoService(contratoService, lancamentoRepository, clock);
    }

    @Test
    void saldoDisponivelEhContratoMenosLancamentosAtivos() {
        Contrato contrato = contrato("100000.00", LocalDate.of(2026, 1, 1), LocalDate.of(2027, 1, 1));
        when(contratoService.buscarEntidade(contratoId)).thenReturn(contrato);
        when(lancamentoRepository.somarPorContratoEStatus(contratoId, StatusLancamento.ATIVO))
                .thenReturn(new BigDecimal("15000.00"));

        SaldoResponseDto saldo = saldoService.saldoDoContrato(contratoId);

        assertThat(saldo.totalLancado()).isEqualByComparingTo("15000.00");
        assertThat(saldo.saldoDisponivel()).isEqualByComparingTo("85000.00");
        assertThat(saldo.percentualConsumido()).isEqualByComparingTo("15.00");
    }

    @Test
    void alertaEhProximoDoLimiteAcimaDe80PorCento() {
        Contrato contrato = contrato("100000.00", LocalDate.of(2026, 1, 1), LocalDate.of(2027, 1, 1));
        when(contratoService.buscarEntidade(contratoId)).thenReturn(contrato);
        when(lancamentoRepository.somarPorContratoEStatus(contratoId, StatusLancamento.ATIVO))
                .thenReturn(new BigDecimal("80000.00"));

        SaldoResponseDto saldo = saldoService.saldoDoContrato(contratoId);

        assertThat(saldo.alerta()).isEqualTo(AlertaSaldo.PROXIMO_DO_LIMITE);
    }

    @Test
    void alertaEhNenhumQuandoConsumoEstaEntre20E80() {
        Contrato contrato = contrato("100000.00", LocalDate.of(2026, 1, 1), LocalDate.of(2027, 1, 1));
        when(contratoService.buscarEntidade(contratoId)).thenReturn(contrato);
        when(lancamentoRepository.somarPorContratoEStatus(contratoId, StatusLancamento.ATIVO))
                .thenReturn(new BigDecimal("50000.00"));

        SaldoResponseDto saldo = saldoService.saldoDoContrato(contratoId);

        assertThat(saldo.alerta()).isEqualTo(AlertaSaldo.NENHUM);
    }

    @Test
    void alertaEhRiscoSubutilizacaoComConsumoBaixoEVigenciaAvancada() {
        // início em 2026-01-01, hoje 2026-11-15: vigência decorrida ~87%
        Contrato contrato = contrato("100000.00", LocalDate.of(2026, 1, 1), LocalDate.of(2027, 1, 1));
        when(contratoService.buscarEntidade(contratoId)).thenReturn(contrato);
        when(lancamentoRepository.somarPorContratoEStatus(contratoId, StatusLancamento.ATIVO))
                .thenReturn(new BigDecimal("5000.00"));

        SaldoResponseDto saldo = saldoService.saldoDoContrato(contratoId);

        assertThat(saldo.alerta()).isEqualTo(AlertaSaldo.RISCO_SUBUTILIZACAO);
    }

    @Test
    void naoAlertaSubutilizacaoQuandoVigenciaAindaEhCurta() {
        // início hoje: vigência decorrida 0%, mesmo com consumo zero não há risco
        Contrato contrato = contrato("100000.00", LocalDate.of(2026, 11, 15), LocalDate.of(2027, 11, 15));
        when(contratoService.buscarEntidade(contratoId)).thenReturn(contrato);
        when(lancamentoRepository.somarPorContratoEStatus(contratoId, StatusLancamento.ATIVO))
                .thenReturn(BigDecimal.ZERO);

        SaldoResponseDto saldo = saldoService.saldoDoContrato(contratoId);

        assertThat(saldo.alerta()).isEqualTo(AlertaSaldo.NENHUM);
    }

    @Test
    void consolidadoSomaTotaisDeTodosOsContratos() {
        Contrato contrato1 = contrato("100000.00", LocalDate.of(2026, 1, 1), LocalDate.of(2027, 1, 1));
        Contrato contrato2 = contrato("50000.00", LocalDate.of(2026, 1, 1), LocalDate.of(2027, 1, 1));
        when(contratoService.listarEntidades()).thenReturn(List.of(contrato1, contrato2));
        when(lancamentoRepository.somarPorContratoEStatus(any(), eq(StatusLancamento.ATIVO)))
                .thenReturn(new BigDecimal("10000.00"));

        SaldoConsolidadoResponseDto consolidado = saldoService.saldoConsolidado();

        assertThat(consolidado.valorTotalContratado()).isEqualByComparingTo("150000.00");
        assertThat(consolidado.totalLancado()).isEqualByComparingTo("20000.00");
        assertThat(consolidado.saldoDisponivel()).isEqualByComparingTo("130000.00");
        assertThat(consolidado.contratos()).hasSize(2);
    }

    private Contrato contrato(String valor, LocalDate inicio, LocalDate fim) {
        Contrato contrato = new Contrato();
        contrato.setId(contratoId);
        contrato.setNome("Contrato Teste");
        contrato.setValorContratado(new BigDecimal(valor));
        contrato.setDataInicio(inicio);
        contrato.setDataFim(fim);
        return contrato;
    }
}