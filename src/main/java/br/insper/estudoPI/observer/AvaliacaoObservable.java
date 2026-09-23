package br.insper.estudoPI.observer;

import br.insper.estudoPI.entity.Avaliacao;

public interface AvaliacaoObservable {
    void notificarObservadores(Avaliacao avaliacao, String evento);
}