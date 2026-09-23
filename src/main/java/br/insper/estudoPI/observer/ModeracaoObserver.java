package br.insper.estudoPI.observer;

import br.insper.estudoPI.entity.Avaliacao;
import org.springframework.stereotype.Component;

@Component
public class ModeracaoObserver implements AvaliacaoObserver {

    public static final String MENSAGEM_NEGATIVA = "MODERACAO - AVALIACAO NEGATIVA";

    @Override
    public void atualizar(Avaliacao avaliacao, String evento) {
        if (!"CRIADO".equals(evento)) {
            return;
        }
        Integer nota = avaliacao.getNota();
        if (nota != null && nota <= 2) {
            System.out.println(MENSAGEM_NEGATIVA
                    + " - Avaliacao ID: " + avaliacao.getId()
                    + " | Autor: " + avaliacao.getAutor()
                    + " | Nota: " + nota
                    + " | Conteudo: " + avaliacao.getConteudo());
        }
    }
}
