package com.example.controle_saldo_api.contrato.controller;

import com.example.controle_saldo_api.contrato.dto.ContratoDto;
import com.example.controle_saldo_api.contrato.dto.ContratoResponseDto;
import com.example.controle_saldo_api.contrato.service.ContratoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/contratos")
@RequiredArgsConstructor
public class ContratoController {

    private final ContratoService contratoService;


    @GetMapping
    public List<ContratoResponseDto> listar() {
        return contratoService.listar();
    }

    @GetMapping("/{id}")
    public ContratoResponseDto buscar(@PathVariable UUID id) {
        return ContratoResponseDto.from(contratoService.buscarEntidade(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ContratoResponseDto criar(@Valid @RequestBody ContratoDto dto) {
        return contratoService.criar(dto);
    }
}
