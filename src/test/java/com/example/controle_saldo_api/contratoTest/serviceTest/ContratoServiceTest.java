package com.example.controle_saldo_api.contratoTest.serviceTest;

import com.example.controle_saldo_api.contrato.dto.ContratoDto;
import com.example.controle_saldo_api.contrato.dto.ContratoResponseDto;
import com.example.controle_saldo_api.contrato.entity.Contrato;
import com.example.controle_saldo_api.contrato.repository.ContratoRepository;
import com.example.controle_saldo_api.contrato.service.ContratoService;
import com.example.controle_saldo_api.shared.exeption.ContratoNaoEncontradoException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
public class ContratoServiceTest {
    @Mock
    private ContratoRepository contratoRepository;

    @InjectMocks
    private ContratoService contratoService;

    @Test
    void criarCalculaDataFimComUmAnoDeVigencia() {
        when(contratoRepository.save(any(Contrato.class))).thenAnswer(inv -> inv.getArgument(0));
        var request = new ContratoDto("Contrato Teste", new BigDecimal("100000.00"), LocalDate.of(2026, 1, 1));

        ContratoResponseDto response = contratoService.criar(request);

        assertThat(response.dataFim()).isEqualTo(LocalDate.of(2027, 1, 1));
    }

    @Test
    void criarComDataBissextaTerminaEm28DeFevereiroDoAnoSeguinte() {
        when(contratoRepository.save(any(Contrato.class))).thenAnswer(inv -> inv.getArgument(0));
        var request = new ContratoDto("Contrato Bissexto", new BigDecimal("50000.00"), LocalDate.of(2028, 2, 29));

        ContratoResponseDto response = contratoService.criar(request);

        assertThat(response.dataFim()).isEqualTo(LocalDate.of(2029, 2, 28));
    }

    @Test
    void criarSalvaContratoComOsDadosDaRequisicao() {
        when(contratoRepository.save(any(Contrato.class))).thenAnswer(inv -> inv.getArgument(0));
        var request = new ContratoDto("Contrato Teste", new BigDecimal("100000.00"), LocalDate.of(2026, 1, 1));

        contratoService.criar(request);

        ArgumentCaptor<Contrato> captor = ArgumentCaptor.forClass(Contrato.class);
        verify(contratoRepository).save(captor.capture());
        Contrato salvo = captor.getValue();
        assertThat(salvo.getNome()).isEqualTo("Contrato Teste");
        assertThat(salvo.getValorContratado()).isEqualByComparingTo("100000.00");
        assertThat(salvo.getDataInicio()).isEqualTo(LocalDate.of(2026, 1, 1));
        assertThat(salvo.getDataFim()).isEqualTo(LocalDate.of(2027, 1, 1));
    }

    @Test
    void criarComValorZeroLancaExcecaoENaoSalva() {
        var request = new ContratoDto("Contrato Zero", BigDecimal.ZERO, LocalDate.of(2026, 1, 1));

        assertThatThrownBy(() -> contratoService.criar(request))
                .isInstanceOf(IllegalArgumentException.class);
        verify(contratoRepository, never()).save(any());
    }

    @Test
    void criarComValorNegativoLancaExcecaoENaoSalva() {
        var request = new ContratoDto("Contrato Negativo", new BigDecimal("-100.00"), LocalDate.of(2026, 1, 1));

        assertThatThrownBy(() -> contratoService.criar(request))
                .isInstanceOf(IllegalArgumentException.class);
        verify(contratoRepository, never()).save(any());
    }

    @Test
    void buscarEntidadeRetornaContratoExistente() {
        UUID id = UUID.randomUUID();
        Contrato contrato = contrato(id, "Contrato Existente");
        when(contratoRepository.findById(id)).thenReturn(Optional.of(contrato));

        Contrato encontrado = contratoService.buscarEntidade(id);

        assertThat(encontrado).isSameAs(contrato);
    }

    @Test
    void buscarEntidadeLancaExcecaoQuandoContratoNaoExiste() {
        UUID id = UUID.randomUUID();
        when(contratoRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> contratoService.buscarEntidade(id))
                .isInstanceOf(ContratoNaoEncontradoException.class)
                .hasMessageContaining(id.toString());
    }

    @Test
    void listarMapeiaContratosParaDto() {
        UUID id = UUID.randomUUID();
        when(contratoRepository.findAll()).thenReturn(List.of(contrato(id, "Contrato Listado")));

        List<ContratoResponseDto> response = contratoService.listar();

        assertThat(response).hasSize(1);
        assertThat(response.get(0).id()).isEqualTo(id);
        assertThat(response.get(0).nome()).isEqualTo("Contrato Listado");
    }

    private Contrato contrato(UUID id, String nome) {
        Contrato contrato = new Contrato();
        contrato.setId(id);
        contrato.setNome(nome);
        contrato.setValorContratado(new BigDecimal("100000.00"));
        contrato.setDataInicio(LocalDate.of(2026, 1, 1));
        contrato.setDataFim(LocalDate.of(2027, 1, 1));
        return contrato;
    }
}

