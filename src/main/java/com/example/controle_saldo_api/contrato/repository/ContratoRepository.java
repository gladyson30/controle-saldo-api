package com.example.controle_saldo_api.contrato.repository;

import com.example.controle_saldo_api.contrato.entity.Contrato;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ContratoRepository extends JpaRepository<Contrato, UUID> {
}
