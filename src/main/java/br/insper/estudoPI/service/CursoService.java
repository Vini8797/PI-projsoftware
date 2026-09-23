package br.insper.estudoPI.service;

import br.insper.estudoPI.dto.CursoDto;
import br.insper.estudoPI.entity.Curso;
import br.insper.estudoPI.exception.CursoNaoEncontradoException;
import br.insper.estudoPI.repository.CursoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

// REGRA DE OURO: todo "if" aqui vira 2 branches que o teste precisa cobrir (o pom exige 100%).
// Mantenha a lógica simples e escreva o teste logo depois de cada método.
@Service
public class CursoService {   // TODO [PADRAO] Observer: "public class CursoService implements CursoObservable"

    @Autowired
    private CursoRepository cursoRepository;

    // TODO [PADRAO] Campos @Autowired dos padrões entram AQUI (ver padroes/PADROES.md)

    // GET /cursos  e  GET /cursos?nome=Jav
    public List<Curso> listar(String nome) {
        if (nome == null || nome.isBlank()) {
            return cursoRepository.findByDeletadoFalse();
        }
        return cursoRepository.findByNomeStartingWithIgnoreCaseAndDeletadoFalse(nome);
    }

    // POST /cursos
    public Curso criar(CursoDto dto) {
        Curso curso = Curso.fromDto(dto);
        return cursoRepository.save(curso);
        // TODO [PADRAO] Observer: salvar em uma variável e chamar notificarObservadores(salvo, "CRIADO")
    }

    // DELETE /cursos/{id}  -> deleção LÓGICA: marca e salva. Nunca deleteById.
    public void deletar(Long id) {
        Curso curso = buscarCurso(id);
        curso.setDeletado(true);
        cursoRepository.save(curso);
        // TODO [PADRAO] Observer: notificarObservadores(curso, "DELETADO")
    }

    // TODO [PROVA] Novos métodos do enunciado entram AQUI (buscar por id, atualizar, mudar status...)

    // TODO [PADRAO] Métodos dos padrões entram AQUI (calcularPrecoFinal, gerarCertificado, notificarObservadores)

    // Reaproveitado por qualquer método que precise "achar ou dar 404"
    private Curso buscarCurso(Long id) {
        return cursoRepository.findById(id)
                .orElseThrow(() -> new CursoNaoEncontradoException("Curso com ID " + id + " não encontrado"));
    }
}
