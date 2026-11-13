package com.example.controle_saldo_api.contrato.service;

import com.example.controle_saldo_api.contrato.dto.ContratoDto;
import com.example.controle_saldo_api.contrato.dto.ContratoResponseDto;
import com.example.controle_saldo_api.contrato.entity.Contrato;
import com.example.controle_saldo_api.contrato.repository.ContratoRepository;
import com.example.controle_saldo_api.shared.exeption.ContratoNaoEncontradoException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class ContratoService {

    private final ContratoRepository contratoRepository;

    @Transactional
    public ContratoResponseDto criar(ContratoDto dto) {
        if (dto.valorContratado() == null || dto.valorContratado().signum() <= 0) {
            throw new IllegalArgumentException("Valor contratado deve ser maior que zero");
        }

        Contrato contrato = new Contrato();
        contrato.setNome(dto.nome());
        contrato.setValorContratado(dto.valorContratado());
        contrato.setDataInicio(dto.dataInicio());
        contrato.setDataFim(dto.dataInicio().plusYears(1));

        Contrato salvo = contratoRepository.save(contrato);
        return ContratoResponseDto.from(salvo);
    }

    public List<ContratoResponseDto> listar() {
        return contratoRepository.findAll()
                .stream()
                .map(ContratoResponseDto::from)
                .toList();
    }

    public Contrato buscarEntidade(UUID id) {
        return contratoRepository.findById(id)
                .orElseThrow(() -> new ContratoNaoEncontradoException(id));
    }

}
