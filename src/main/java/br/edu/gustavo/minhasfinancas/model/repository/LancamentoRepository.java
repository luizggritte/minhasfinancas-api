package br.edu.gustavo.minhasfinancas.model.repository;

import br.edu.gustavo.minhasfinancas.model.enums.StatusLancamento;
import br.edu.gustavo.minhasfinancas.model.enums.TipoLancamento;
import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.gustavo.minhasfinancas.model.entity.Lancamento;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;

public interface LancamentoRepository extends JpaRepository<Lancamento, Long> {

    @Query( value =
            "select sum(l.valor) from Lancamento l join l.usuario u " +
            "where u.id = :idUsuario and l.tipo = :tipo and l.status = :status group by u")
    BigDecimal obterSaldoPorTipoLancamentoEUsuarioEStatus(
            @Param("idUsuario") Long idUsuario,
            @Param("tipo") TipoLancamento tipo,
            @Param("status")StatusLancamento status
    );
}
