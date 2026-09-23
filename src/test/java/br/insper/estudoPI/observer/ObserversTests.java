package br.insper.estudoPI.observer;

import br.insper.estudoPI.entity.Auditoria;
import br.insper.estudoPI.entity.Avaliacao;
import br.insper.estudoPI.repository.AuditoriaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

@ExtendWith(MockitoExtension.class)
public class ObserversTests {

    @InjectMocks
    private AuditoriaObserver auditoriaObserver;

    @Mock
    private AuditoriaRepository auditoriaRepository;

    private final ModeracaoObserver moderacaoObserver = new ModeracaoObserver();

    private final ByteArrayOutputStream saida = new ByteArrayOutputStream();
    private final PrintStream saidaOriginal = System.out;

    @BeforeEach
    void capturarSaida() {
        System.setOut(new PrintStream(saida));
    }

    @AfterEach
    void restaurarSaida() {
        System.setOut(saidaOriginal);
    }

    private Avaliacao criarAvaliacao(Integer nota) {
        Avaliacao avaliacao = new Avaliacao();
        avaliacao.setId(1L);
        avaliacao.setAutor("James Bond");
        avaliacao.setConteudo("Servico pessimo");
        avaliacao.setNota(nota);
        return avaliacao;
    }

    @Test
    public void test_auditoriaDevePersistirEventoCriado() {
        auditoriaObserver.atualizar(criarAvaliacao(5), "CRIADO");

        ArgumentCaptor<Auditoria> captor = ArgumentCaptor.forClass(Auditoria.class);
        Mockito.verify(auditoriaRepository).save(captor.capture());

        Auditoria salvo = captor.getValue();
        Assertions.assertEquals("CRIADO", salvo.getTipoOperacao());
        Assertions.assertEquals(1L, salvo.getAvaliacaoId());
        Assertions.assertNotNull(salvo.getTimestamp());
    }

    @Test
    public void test_auditoriaDevePersistirEventoDeletado() {
        auditoriaObserver.atualizar(criarAvaliacao(5), "DELETADO");

        ArgumentCaptor<Auditoria> captor = ArgumentCaptor.forClass(Auditoria.class);
        Mockito.verify(auditoriaRepository).save(captor.capture());

        Assertions.assertEquals("DELETADO", captor.getValue().getTipoOperacao());
        Assertions.assertNotNull(captor.getValue().getTimestamp());
    }

    @Test
    public void test_moderacaoDeveLogarQuandoNotaEhNegativa() {
        moderacaoObserver.atualizar(criarAvaliacao(1), "CRIADO");
        moderacaoObserver.atualizar(criarAvaliacao(2), "CRIADO");

        String impresso = saida.toString();
        Assertions.assertTrue(impresso.contains(ModeracaoObserver.MENSAGEM_NEGATIVA));
        Assertions.assertTrue(impresso.contains("Nota: 1"));
        Assertions.assertTrue(impresso.contains("Nota: 2"));
        Assertions.assertTrue(impresso.contains("James Bond"));
    }

    @Test
    public void test_moderacaoNaoDeveLogarQuandoNotaNaoEhNegativa() {
        moderacaoObserver.atualizar(criarAvaliacao(3), "CRIADO");
        moderacaoObserver.atualizar(criarAvaliacao(null), "CRIADO");

        Assertions.assertFalse(saida.toString().contains(ModeracaoObserver.MENSAGEM_NEGATIVA));
    }

    @Test
    public void test_moderacaoNaoDeveLogarQuandoEventoNaoEhCriacao() {
        moderacaoObserver.atualizar(criarAvaliacao(1), "DELETADO");

        Assertions.assertFalse(saida.toString().contains(ModeracaoObserver.MENSAGEM_NEGATIVA));
    }
}
