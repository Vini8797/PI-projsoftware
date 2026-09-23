package br.insper.estudoPI.service;

import br.insper.estudoPI.dto.AvaliacaoDto;
import br.insper.estudoPI.entity.Avaliacao;
import br.insper.estudoPI.exception.AvaliacaoNaoEncontradaException;
import br.insper.estudoPI.observer.AvaliacaoObservable;
import br.insper.estudoPI.observer.AvaliacaoObserver;
import br.insper.estudoPI.repository.AvaliacaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AvaliacaoService implements AvaliacaoObservable {

    @Autowired
    private AvaliacaoRepository avaliacaoRepository;

    @Autowired(required = false)
    private List<AvaliacaoObserver> observers = new ArrayList<>();

    public List<Avaliacao> listar(String autor) {
        if (autor == null || autor.isBlank()) {
            return avaliacaoRepository.findByDeletadoFalse();
        }
        return avaliacaoRepository.findByAutorStartingWithIgnoreCaseAndDeletadoFalse(autor);
    }

    public Avaliacao obterPorId(Long id) {
        return buscarAvaliacao(id);
    }

    public Avaliacao criar(AvaliacaoDto dto) {
        Avaliacao avaliacao = Avaliacao.fromDto(dto);
        Avaliacao salvo = avaliacaoRepository.save(avaliacao);
        notificarObservadores(salvo, "CRIADO");
        return salvo;
    }

    public void deletar(Long id) {
        Avaliacao avaliacao = buscarAvaliacao(id);
        avaliacao.setDeletado(true);
        avaliacaoRepository.save(avaliacao);
        notificarObservadores(avaliacao, "DELETADO");
    }

    @Override
    public void notificarObservadores(Avaliacao avaliacao, String evento) {
        for (AvaliacaoObserver observer : observers) {
            observer.atualizar(avaliacao, evento);
        }
    }

    private Avaliacao buscarAvaliacao(Long id) {
        return avaliacaoRepository.findById(id)
                .orElseThrow(() -> new AvaliacaoNaoEncontradaException("Avaliação com ID " + id + " não encontrada"));
    }
}
