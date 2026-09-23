package br.insper.estudoPI.observer;

import br.insper.estudoPI.entity.Avaliacao;

public interface AvaliacaoObserver {
    void atualizar(Avaliacao avaliacao, String evento);
}