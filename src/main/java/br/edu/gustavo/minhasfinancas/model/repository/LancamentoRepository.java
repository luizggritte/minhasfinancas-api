package br.edu.gustavo.minhasfinancas.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.gustavo.minhasfinancas.model.entity.Lancamento;

public interface LancamentoRepository extends JpaRepository<Lancamento, Long> {

}
