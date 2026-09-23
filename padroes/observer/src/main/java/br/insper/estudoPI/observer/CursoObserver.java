package br.insper.estudoPI.observer;

import br.insper.estudoPI.entity.Curso;

// OBSERVER: quem quer ser AVISADO implementa isto.
public interface CursoObserver {
    void atualizar(Curso curso, String evento);
}
