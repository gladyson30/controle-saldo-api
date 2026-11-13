package com.example.controle_saldo_api.shared.exeption;

import java.util.UUID;

public class ContratoNaoEncontradoException extends RuntimeException {
    public ContratoNaoEncontradoException(UUID id) {
        super("Contrato não encontrado: " + id);
    }
}
