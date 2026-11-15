package com.example.controle_saldo_api.shared.exeption;

import java.math.BigDecimal;

public class SaldoInsuficienteException extends RuntimeException {

    public SaldoInsuficienteException(BigDecimal disponivel, BigDecimal solicitado) {
        super("Saldo insuficiente. Disponível: " + disponivel + ", solicitado: " + solicitado);
    }
}
