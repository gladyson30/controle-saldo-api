package com.example.controle_saldo_api.shared.exeption;

public class LancamentoNaoEncontradoException extends RuntimeException {
  public LancamentoNaoEncontradoException(String message) {
    super(message);
  }
}
