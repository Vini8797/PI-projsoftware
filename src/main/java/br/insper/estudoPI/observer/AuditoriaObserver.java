package br.insper.estudoPI.observer;

import br.insper.estudoPI.entity.Auditoria;
import br.insper.estudoPI.entity.Avaliacao;
import br.insper.estudoPI.repository.AuditoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AuditoriaObserver implements AvaliacaoObserver {

    @Autowired
    private AuditoriaRepository auditoriaRepository;

    @Override
    public void atualizar(Avaliacao avaliacao, String evento) {
        auditoriaRepository.save(Auditoria.de(avaliacao.getId(), evento));
    }
}
