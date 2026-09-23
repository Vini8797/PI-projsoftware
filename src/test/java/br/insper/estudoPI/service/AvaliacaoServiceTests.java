package br.insper.estudoPI.service;

import br.insper.estudoPI.dto.AvaliacaoDto;
import br.insper.estudoPI.entity.Avaliacao;
import br.insper.estudoPI.exception.AvaliacaoNaoEncontradaException;
import br.insper.estudoPI.observer.AvaliacaoObserver;
import br.insper.estudoPI.repository.AvaliacaoRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@ExtendWith(MockitoExtension.class)
public class AvaliacaoServiceTests {

    @InjectMocks
    private AvaliacaoService avaliacaoService;

    @Mock
    private AvaliacaoRepository avaliacaoRepository;

    @Mock
    private AvaliacaoObserver observer;

    // O @InjectMocks não sabe preencher uma List. Então colocamos na mão
    // uma lista com o observer falso dentro do campo "observers" do service.
    @BeforeEach
    void configurarObservers() {
        ReflectionTestUtils.setField(avaliacaoService, "observers", List.of(observer));
    }

    @Test
    public void test_shouldReturnAllAvaliacoesWhenFiltroIsNull() {
        List<Avaliacao> avaliacoes = new ArrayList<>();
        avaliacoes.add(new Avaliacao());
        avaliacoes.add(new Avaliacao());

        Mockito.when(avaliacaoRepository.findByDeletadoFalse()).thenReturn(avaliacoes);

        List<Avaliacao> response = avaliacaoService.listar(null);

        Assertions.assertEquals(2, response.size());
        Mockito.verify(avaliacaoRepository).findByDeletadoFalse();
    }

    @Test
    public void test_shouldReturnAllAvaliacoesWhenFiltroIsBlank() {
        Mockito.when(avaliacaoRepository.findByDeletadoFalse()).thenReturn(new ArrayList<>());

        List<Avaliacao> response = avaliacaoService.listar("   ");

        Assertions.assertEquals(0, response.size());
        Mockito.verify(avaliacaoRepository).findByDeletadoFalse();
    }

    @Test
    public void test_shouldReturnFilteredAvaliacoesWhenFiltroIsNotBlank() {
        Avaliacao avaliacao = new Avaliacao();
        avaliacao.setAutor("James Bond");

        List<Avaliacao> avaliacoes = new ArrayList<>();
        avaliacoes.add(avaliacao);

        Mockito.when(avaliacaoRepository.findByAutorStartingWithIgnoreCaseAndDeletadoFalse("James"))
                .thenReturn(avaliacoes);

        List<Avaliacao> response = avaliacaoService.listar("James");

        Assertions.assertEquals(1, response.size());
        Assertions.assertEquals("James Bond", response.get(0).getAutor());
    }

    @Test
    public void test_shouldReturnAvaliacaoWhenIdExists() {
        Avaliacao avaliacao = new Avaliacao();
        avaliacao.setId(1L);
        avaliacao.setAutor("James Bond");

        Mockito.when(avaliacaoRepository.findById(1L)).thenReturn(Optional.of(avaliacao));

        Avaliacao response = avaliacaoService.obterPorId(1L);

        Assertions.assertEquals(1L, response.getId());
        Assertions.assertEquals("James Bond", response.getAutor());
    }

    @Test
    public void test_shouldThrowExceptionWhenObterPorIdDoesNotExist() {
        Mockito.when(avaliacaoRepository.findById(99L)).thenReturn(Optional.empty());

        AvaliacaoNaoEncontradaException exception = Assertions.assertThrows(
                AvaliacaoNaoEncontradaException.class,
                () -> avaliacaoService.obterPorId(99L)
        );

        Assertions.assertEquals("Avaliação com ID 99 não encontrada", exception.getMessage());
    }

    @Test
    public void test_shouldCreateAvaliacaoWhenDtoIsValid() {
        AvaliacaoDto dto = new AvaliacaoDto();
        dto.setAutor("James Bond");
        dto.setConteudo("James Bond e a fuga dos códigos");
        dto.setNota(5);

        Avaliacao avaliacao = Avaliacao.fromDto(dto);
        avaliacao.setId(1L);

        Mockito.when(avaliacaoRepository.save(Mockito.any(Avaliacao.class))).thenReturn(avaliacao);

        Avaliacao response = avaliacaoService.criar(dto);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(1L, response.getId());
        Assertions.assertEquals("James Bond", response.getAutor());
        Assertions.assertEquals(5, response.getNota());
        Assertions.assertFalse(response.isDeletado());
        Assertions.assertNotNull(response.getDataAvaliacao());

        // Observer: o service avisou os inscritos com o objeto JÁ salvo (com id)
        Mockito.verify(observer).atualizar(avaliacao, "CRIADO");
    }

    @Test
    public void test_shouldSetDeletadoTrueWhenAvaliacaoExists() {
        Avaliacao avaliacao = new Avaliacao();
        avaliacao.setId(1L);
        avaliacao.setAutor("James Bond");
        avaliacao.setDeletado(false);

        Mockito.when(avaliacaoRepository.findById(1L)).thenReturn(Optional.of(avaliacao));

        avaliacaoService.deletar(1L);

        ArgumentCaptor<Avaliacao> captor = ArgumentCaptor.forClass(Avaliacao.class);
        Mockito.verify(avaliacaoRepository).save(captor.capture());

        Assertions.assertTrue(captor.getValue().isDeletado());
        Mockito.verify(avaliacaoRepository, Mockito.never()).deleteById(Mockito.anyLong());
        Mockito.verify(observer).atualizar(avaliacao, "DELETADO");
    }

    @Test
    public void test_shouldThrowExceptionWhenAvaliacaoDoesNotExist() {
        Mockito.when(avaliacaoRepository.findById(99L)).thenReturn(Optional.empty());

        AvaliacaoNaoEncontradaException exception = Assertions.assertThrows(
                AvaliacaoNaoEncontradaException.class,
                () -> avaliacaoService.deletar(99L)
        );

        Assertions.assertEquals("Avaliação com ID 99 não encontrada", exception.getMessage());
        Mockito.verify(avaliacaoRepository, Mockito.never()).save(Mockito.any());
        Mockito.verify(observer, Mockito.never()).atualizar(Mockito.any(), Mockito.any());
    }

}
