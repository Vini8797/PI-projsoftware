package br.insper.estudoPI.repository;

import br.insper.estudoPI.entity.Avaliacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AvaliacaoRepository extends JpaRepository<Avaliacao, Long> {

    List<Avaliacao> findByDeletadoFalse();
    List<Avaliacao> findByAutorStartingWithIgnoreCaseAndDeletadoFalse(String autor);
}
