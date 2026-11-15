package com.example.controle_saldo_api.lancamento.service;

import com.example.controle_saldo_api.contrato.entity.Contrato;
import com.example.controle_saldo_api.contrato.service.ContratoService;
import com.example.controle_saldo_api.lancamento.dto.EditarLancamentoRequestDto;
import com.example.controle_saldo_api.lancamento.dto.LancamentoDto;
import com.example.controle_saldo_api.lancamento.dto.LancamentoResponseDto;
import com.example.controle_saldo_api.lancamento.entity.Lancamento;
import com.example.controle_saldo_api.lancamento.entity.StatusLancamento;
import com.example.controle_saldo_api.lancamento.repository.LancamentoRepository;
import com.example.controle_saldo_api.shared.exeption.LancamentoNaoEncontradoException;
import com.example.controle_saldo_api.shared.exeption.SaldoInsuficienteException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class LancamentoService {

    private final LancamentoRepository lancamentoRepository;
    private final ContratoService contratoService;


    @Transactional
    public LancamentoResponseDto registrar(LancamentoDto request) {
        Contrato contrato = contratoService.buscarComLock(request.contratoId());

        BigDecimal saldoDisponivel = calcularSaldoDisponivel(contrato);
        if (request.valor().compareTo(saldoDisponivel) > 0) {
            throw new SaldoInsuficienteException(saldoDisponivel, request.valor());
        }

        Lancamento lancamento = new Lancamento();
        lancamento.setContrato(contrato);
        lancamento.setData(request.data());
        lancamento.setValor(request.valor());
        lancamento.setStatus(StatusLancamento.ATIVO);

        return LancamentoResponseDto.from(lancamentoRepository.save(lancamento));
    }

    @Transactional
    public LancamentoResponseDto editar(UUID id, EditarLancamentoRequestDto request) {
        UUID contratoId = lancamentoRepository.buscarContratoId(id)
                .orElseThrow(() -> new LancamentoNaoEncontradoException(id));

        Contrato contrato = contratoService.buscarComLock(contratoId);
        Lancamento lancamento = buscarPorId(id);

        if (lancamento.getStatus() == StatusLancamento.ESTORNADO) {
            throw new IllegalStateException("Lançamento estornado não pode ser editado");
        }

        BigDecimal saldoDisponivel = calcularSaldoDisponivel(contrato).add(lancamento.getValor());
        if (request.valor().compareTo(saldoDisponivel) > 0) {
            throw new SaldoInsuficienteException(saldoDisponivel, request.valor());
        }

        lancamento.setData(request.data());
        lancamento.setValor(request.valor());

        return LancamentoResponseDto.from(lancamento);
    }

    private BigDecimal calcularSaldoDisponivel(Contrato contrato) {
        BigDecimal totalLancado = lancamentoRepository.somarPorContratoEStatus(
                contrato.getId(), StatusLancamento.ATIVO);
        return contrato.getValorContratado().subtract(totalLancado);
    }

    public List<LancamentoResponseDto> listarPorContrato(UUID contratoId) {
        contratoService.buscarEntidade(contratoId);

        return lancamentoRepository.findByContratoIdOrderByDataAsc(contratoId)
                .stream()
                .map(LancamentoResponseDto::from)
                .toList();
    }
    @Transactional
    public LancamentoResponseDto estornar(UUID id, String motivo) {
        Lancamento lancamento = buscarPorId(id);

        if (lancamento.getStatus() == StatusLancamento.ESTORNADO) {
            throw new IllegalStateException("Lançamento já está estornado");
        }

        lancamento.setStatus(StatusLancamento.ESTORNADO);
        lancamento.setDataEstorno(LocalDateTime.now());
        lancamento.setMotivoEstorno(motivo);

        return LancamentoResponseDto.from(lancamento);
    }

    private Lancamento buscarPorId(UUID id) {
        return lancamentoRepository.findById(id)
                .orElseThrow(() -> new LancamentoNaoEncontradoException(id));
    }
}
