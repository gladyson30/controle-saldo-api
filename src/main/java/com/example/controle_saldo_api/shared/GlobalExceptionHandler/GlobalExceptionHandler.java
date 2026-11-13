package com.example.controle_saldo_api.shared.GlobalExceptionHandler;

import com.example.controle_saldo_api.shared.exeption.ContratoNaoEncontradoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ContratoNaoEncontradoException.class)
    public ProblemDetail handleContratoNaoEncontrado(ContratoNaoEncontradoException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }
}
