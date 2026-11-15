package com.example.controle_saldo_api.shared.exeption;

import java.util.UUID;

public class LancamentoNaoEncontradoException extends RuntimeException {
    public LancamentoNaoEncontradoException(UUID id) {
        super("Lançamento não encontrado: " + id);
    }
}
