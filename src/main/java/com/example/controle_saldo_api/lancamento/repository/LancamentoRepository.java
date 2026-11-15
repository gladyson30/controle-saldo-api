package com.example.controle_saldo_api.lancamento.repository;

import com.example.controle_saldo_api.lancamento.entity.Lancamento;
import com.example.controle_saldo_api.lancamento.entity.StatusLancamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LancamentoRepository extends JpaRepository<Lancamento, UUID> {
    List<Lancamento> findByContratoIdOrderByDataAsc(UUID contratoId);

    @Query("""
        select coalesce(sum(l.valor), 0)
        from Lancamento l
        where l.contrato.id = :contratoId
          and l.status = :status
        """)
    BigDecimal somarPorContratoEStatus(@Param("contratoId") UUID contratoId,
                                       @Param("status") StatusLancamento status);
    @Query("select l.contrato.id from Lancamento l where l.id = :id")
    Optional<UUID> buscarContratoId(@Param("id") UUID id);
}
