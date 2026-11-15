package com.example.controle_saldo_api.shared.exeption;

public class SaldoInsuficienteException extends RuntimeException {
  public SaldoInsuficienteException(String message) {
    super(message);
  }
}
