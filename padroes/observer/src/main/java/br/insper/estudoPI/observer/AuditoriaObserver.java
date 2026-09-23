package br.insper.estudoPI.observer;

import br.insper.estudoPI.entity.Curso;
import org.springframework.stereotype.Component;

// @Component -> o Spring encontra e coloca automaticamente na List<CursoObserver> do service.
// Para criar um observer novo: nova classe + implements CursoObserver + @Component. O service não muda.
@Component
public class AuditoriaObserver implements CursoObserver {

    @Override
    public void atualizar(Curso curso, String evento) {
        System.out.println("AUDITORIA - Curso ID: " + curso.getId()
                + " | Evento: " + evento
                + " | Nome: " + curso.getNome());
    }
}
