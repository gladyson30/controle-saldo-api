package com.example.controle_saldo_api.lancamentoTest.serviceTest;
import com.example.controle_saldo_api.contrato.entity.Contrato;
import com.example.controle_saldo_api.contrato.service.ContratoService;
import com.example.controle_saldo_api.lancamento.dto.EditarLancamentoRequestDto;
import com.example.controle_saldo_api.lancamento.dto.LancamentoDto;
import com.example.controle_saldo_api.lancamento.dto.LancamentoResponseDto;
import com.example.controle_saldo_api.lancamento.entity.Lancamento;
import com.example.controle_saldo_api.lancamento.entity.StatusLancamento;
import com.example.controle_saldo_api.lancamento.repository.LancamentoRepository;
import com.example.controle_saldo_api.lancamento.service.LancamentoService;
import com.example.controle_saldo_api.shared.exeption.ContratoNaoEncontradoException;
import com.example.controle_saldo_api.shared.exeption.LancamentoNaoEncontradoException;
import com.example.controle_saldo_api.shared.exeption.SaldoInsuficienteException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LancamentoServiceTest {
    @Mock
    private LancamentoRepository lancamentoRepository;

    @Mock
    private ContratoService contratoService;

    @InjectMocks
    private LancamentoService lancamentoService;

    private final UUID contratoId = UUID.randomUUID();
    private final UUID lancamentoId = UUID.randomUUID();

    // registrar

    @Test
    void registrarSalvaLancamentoAtivoQuandoHaSaldo() {
        Contrato contrato = contrato("100000.00");
        when(contratoService.buscarComLock(contratoId)).thenReturn(contrato);
        when(lancamentoRepository.somarPorContratoEStatus(contratoId, StatusLancamento.ATIVO))
                .thenReturn(new BigDecimal("15000.00"));
        when(lancamentoRepository.save(any(Lancamento.class))).thenAnswer(inv -> inv.getArgument(0));

        var request = new LancamentoDto(contratoId, LocalDate.of(2026, 3, 1), new BigDecimal("20000.00"));

        LancamentoResponseDto response = lancamentoService.registrar(request);

        assertThat(response.status()).isEqualTo(StatusLancamento.ATIVO);
        assertThat(response.valor()).isEqualByComparingTo("20000.00");
        assertThat(response.contratoId()).isEqualTo(contratoId);
    }

    @Test
    void registrarAceitaValorIgualAoSaldoDisponivel() {
        Contrato contrato = contrato("100000.00");
        when(contratoService.buscarComLock(contratoId)).thenReturn(contrato);
        when(lancamentoRepository.somarPorContratoEStatus(contratoId, StatusLancamento.ATIVO))
                .thenReturn(new BigDecimal("15000.00"));
        when(lancamentoRepository.save(any(Lancamento.class))).thenAnswer(inv -> inv.getArgument(0));

        var request = new LancamentoDto(contratoId, LocalDate.of(2026, 3, 1), new BigDecimal("85000.00"));

        LancamentoResponseDto response = lancamentoService.registrar(request);

        assertThat(response.valor()).isEqualByComparingTo("85000.00");
    }

    @Test
    void registrarBloqueiaValorAcimaDoSaldoDisponivel() {
        Contrato contrato = contrato("100000.00");
        when(contratoService.buscarComLock(contratoId)).thenReturn(contrato);
        when(lancamentoRepository.somarPorContratoEStatus(contratoId, StatusLancamento.ATIVO))
                .thenReturn(new BigDecimal("15000.00"));

        var request = new LancamentoDto(contratoId, LocalDate.of(2026, 3, 1), new BigDecimal("85000.01"));

        assertThatThrownBy(() -> lancamentoService.registrar(request))
                .isInstanceOf(SaldoInsuficienteException.class);
        verify(lancamentoRepository, never()).save(any());
    }

    // editar

    @Test
    void editarConsideraOValorAntigoNoSaldoDisponivel() {
        Contrato contrato = contrato("100000.00");
        Lancamento existente = lancamento(contrato, "15000.00", StatusLancamento.ATIVO);
        when(lancamentoRepository.buscarContratoId(lancamentoId)).thenReturn(Optional.of(contratoId));
        when(contratoService.buscarComLock(contratoId)).thenReturn(contrato);
        when(lancamentoRepository.findById(lancamentoId)).thenReturn(Optional.of(existente));
        when(lancamentoRepository.somarPorContratoEStatus(contratoId, StatusLancamento.ATIVO))
                .thenReturn(new BigDecimal("15000.00"));

        // saldo sem o lançamento: 85.000; com o valor antigo devolvido: 100.000
        var request = new EditarLancamentoRequestDto(LocalDate.of(2026, 3, 10), new BigDecimal("100000.00"));

        LancamentoResponseDto response = lancamentoService.editar(lancamentoId, request);

        assertThat(response.valor()).isEqualByComparingTo("100000.00");
    }

    @Test
    void editarBloqueiaValorAcimaDoSaldoConsiderandoValorAntigo() {
        Contrato contrato = contrato("100000.00");
        Lancamento existente = lancamento(contrato, "15000.00", StatusLancamento.ATIVO);
        when(lancamentoRepository.buscarContratoId(lancamentoId)).thenReturn(Optional.of(contratoId));
        when(contratoService.buscarComLock(contratoId)).thenReturn(contrato);
        when(lancamentoRepository.findById(lancamentoId)).thenReturn(Optional.of(existente));
        when(lancamentoRepository.somarPorContratoEStatus(contratoId, StatusLancamento.ATIVO))
                .thenReturn(new BigDecimal("15000.00"));

        var request = new EditarLancamentoRequestDto(LocalDate.of(2026, 3, 10), new BigDecimal("100000.01"));

        assertThatThrownBy(() -> lancamentoService.editar(lancamentoId, request))
                .isInstanceOf(SaldoInsuficienteException.class);
    }

    @Test
    void editarLancamentoEstornadoLancaExcecao() {
        Contrato contrato = contrato("100000.00");
        Lancamento estornado = lancamento(contrato, "15000.00", StatusLancamento.ESTORNADO);
        when(lancamentoRepository.buscarContratoId(lancamentoId)).thenReturn(Optional.of(contratoId));
        when(contratoService.buscarComLock(contratoId)).thenReturn(contrato);
        when(lancamentoRepository.findById(lancamentoId)).thenReturn(Optional.of(estornado));

        var request = new EditarLancamentoRequestDto(LocalDate.of(2026, 3, 10), new BigDecimal("20000.00"));

        assertThatThrownBy(() -> lancamentoService.editar(lancamentoId, request))
                .isInstanceOf(IllegalStateException.class);
        verify(lancamentoRepository, never()).somarPorContratoEStatus(any(), any());
    }

    @Test
    void editarLancamentoInexistenteLancaExcecaoSemTravarContrato() {
        when(lancamentoRepository.buscarContratoId(lancamentoId)).thenReturn(Optional.empty());

        var request = new EditarLancamentoRequestDto(LocalDate.of(2026, 3, 10), new BigDecimal("20000.00"));

        assertThatThrownBy(() -> lancamentoService.editar(lancamentoId, request))
                .isInstanceOf(LancamentoNaoEncontradoException.class);
        verify(contratoService, never()).buscarComLock(any());
    }

    // estornar

    @Test
    void estornarMudaStatusEPreencheDataEMotivo() {
        Contrato contrato = contrato("100000.00");
        Lancamento ativo = lancamento(contrato, "15000.00", StatusLancamento.ATIVO);
        when(lancamentoRepository.findById(lancamentoId)).thenReturn(Optional.of(ativo));

        LancamentoResponseDto response = lancamentoService.estornar(lancamentoId, "cancelamento do pedido");

        assertThat(response.status()).isEqualTo(StatusLancamento.ESTORNADO);
        assertThat(response.dataEstorno()).isNotNull();
        assertThat(response.motivoEstorno()).isEqualTo("cancelamento do pedido");
    }

    @Test
    void estornarAceitaMotivoNulo() {
        Contrato contrato = contrato("100000.00");
        Lancamento ativo = lancamento(contrato, "15000.00", StatusLancamento.ATIVO);
        when(lancamentoRepository.findById(lancamentoId)).thenReturn(Optional.of(ativo));

        LancamentoResponseDto response = lancamentoService.estornar(lancamentoId, null);

        assertThat(response.status()).isEqualTo(StatusLancamento.ESTORNADO);
        assertThat(response.motivoEstorno()).isNull();
    }

    @Test
    void estornarJaEstornadoLancaExcecao() {
        Contrato contrato = contrato("100000.00");
        Lancamento estornado = lancamento(contrato, "15000.00", StatusLancamento.ESTORNADO);
        when(lancamentoRepository.findById(lancamentoId)).thenReturn(Optional.of(estornado));

        assertThatThrownBy(() -> lancamentoService.estornar(lancamentoId, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void estornarInexistenteLancaExcecao() {
        when(lancamentoRepository.findById(lancamentoId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> lancamentoService.estornar(lancamentoId, null))
                .isInstanceOf(LancamentoNaoEncontradoException.class);
    }

    // listarPorContrato

    @Test
    void listarPorContratoMapeiaLancamentosDoContrato() {
        Contrato contrato = contrato("100000.00");
        Lancamento primeiro = lancamento(contrato, "15000.00", StatusLancamento.ATIVO);
        Lancamento segundo = lancamento(contrato, "5000.00", StatusLancamento.ESTORNADO);
        when(contratoService.buscarEntidade(contratoId)).thenReturn(contrato);
        when(lancamentoRepository.findByContratoIdOrderByDataAsc(contratoId))
                .thenReturn(List.of(primeiro, segundo));

        List<LancamentoResponseDto> response = lancamentoService.listarPorContrato(contratoId);

        assertThat(response).hasSize(2);
        assertThat(response.get(0).valor()).isEqualByComparingTo("15000.00");
        assertThat(response.get(1).status()).isEqualTo(StatusLancamento.ESTORNADO);
    }

    @Test
    void listarPorContratoInexistenteLancaExcecaoSemConsultarLancamentos() {
        when(contratoService.buscarEntidade(contratoId))
                .thenThrow(new ContratoNaoEncontradoException(contratoId));

        assertThatThrownBy(() -> lancamentoService.listarPorContrato(contratoId))
                .isInstanceOf(ContratoNaoEncontradoException.class);
        verify(lancamentoRepository, never()).findByContratoIdOrderByDataAsc(any());
    }

    // auxiliares

    private Contrato contrato(String valorContratado) {
        Contrato contrato = new Contrato();
        contrato.setId(contratoId);
        contrato.setNome("Contrato Teste");
        contrato.setValorContratado(new BigDecimal(valorContratado));
        contrato.setDataInicio(LocalDate.of(2026, 1, 1));
        contrato.setDataFim(LocalDate.of(2027, 1, 1));
        return contrato;
    }

    private Lancamento lancamento(Contrato contrato, String valor, StatusLancamento status) {
        Lancamento lancamento = new Lancamento();
        lancamento.setId(lancamentoId);
        lancamento.setContrato(contrato);
        lancamento.setData(LocalDate.of(2026, 3, 1));
        lancamento.setValor(new BigDecimal(valor));
        lancamento.setStatus(status);
        return lancamento;
    }
}


