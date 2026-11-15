package com.example.controle_saldo_api.lancamento.controller;


import com.example.controle_saldo_api.lancamento.dto.EditarLancamentoRequestDto;
import com.example.controle_saldo_api.lancamento.dto.LancamentoDto;
import com.example.controle_saldo_api.lancamento.dto.LancamentoResponseDto;
import com.example.controle_saldo_api.lancamento.service.LancamentoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequestMapping("/lancamento")
@RestController
@RequiredArgsConstructor
public class LancamentoController {

    private final LancamentoService lancamentoService;

    @PostMapping("/registrar")
    @ResponseStatus(HttpStatus.CREATED)
    public LancamentoResponseDto registrar(@Valid @RequestBody LancamentoDto request) {
        return lancamentoService.registrar(request);
    }

    @GetMapping("/listar/{contratoId}")
    public List<LancamentoResponseDto> listarPorContrato(@PathVariable UUID contratoId) {
        return lancamentoService.listarPorContrato(contratoId);
    }
    @PutMapping("/editar/{id}")
    public LancamentoResponseDto editar(@PathVariable UUID id,
                                        @Valid @RequestBody EditarLancamentoRequestDto request) {
        return lancamentoService.editar(id, request);
    }

    @PostMapping("/estorno/{id}")
    public LancamentoResponseDto estornar(@PathVariable UUID id) {
        return lancamentoService.estornar(id, null);
    }
}
