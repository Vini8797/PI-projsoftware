package br.insper.estudoPI.observer;

import br.insper.estudoPI.entity.Curso;

// OBSERVABLE (ou "subject"): quem AVISA implementa isto. No nosso caso, o CursoService.
public interface CursoObservable {
    void notificarObservadores(Curso curso, String evento);
}
