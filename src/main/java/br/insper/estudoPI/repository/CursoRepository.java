package br.insper.estudoPI.repository;

import br.insper.estudoPI.entity.Curso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

// É uma interface: o Spring lê o NOME do método e gera o SQL sozinho.
// De graça, o JpaRepository já dá: save, findById, findAll, deleteAll, existsById, count.
@Repository
public interface CursoRepository extends JpaRepository<Curso, Long> {

    // SELECT * FROM cursos WHERE deletado = false
    List<Curso> findByDeletadoFalse();

    // SELECT * FROM cursos WHERE UPPER(nome) LIKE UPPER('texto%') AND deletado = false
    // O nome do método depende do nome do CAMPO na entidade ("Nome" -> campo "nome").
    List<Curso> findByNomeStartingWithIgnoreCaseAndDeletadoFalse(String nome);

    // TODO [PROVA] Novas consultas entram aqui. Palavras-chave úteis:
    //   StartingWith (texto%) | Containing (%texto%) | EndingWith (%texto)
    //   IgnoreCase | GreaterThan | LessThan | Between | OrderByNomeAsc
    //   Ex: List<Curso> findByCategoriaAndDeletadoFalse(String categoria);
}
