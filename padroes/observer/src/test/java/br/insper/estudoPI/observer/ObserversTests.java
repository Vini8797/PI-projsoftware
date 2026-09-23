package br.insper.estudoPI.observer;

import br.insper.estudoPI.entity.Curso;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

// Os observers só imprimem. Para testar, desviamos o System.out para uma variável e lemos o que saiu.
public class ObserversTests {

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

    private Curso criarCurso() {
        Curso curso = new Curso();
        curso.setId(1L);
        curso.setNome("Java Basico");
        return curso;
    }

    @Test
    public void test_auditoriaDeveImprimirIdEEvento() {
        new AuditoriaObserver().atualizar(criarCurso(), "CRIADO");

        String impresso = saida.toString();
        Assertions.assertTrue(impresso.contains("AUDITORIA"));
        Assertions.assertTrue(impresso.contains("Curso ID: 1"));
        Assertions.assertTrue(impresso.contains("CRIADO"));
    }

    @Test
    public void test_emailDeveImprimirNomeEEvento() {
        new EmailObserver().atualizar(criarCurso(), "DELETADO");

        String impresso = saida.toString();
        Assertions.assertTrue(impresso.contains("EMAIL ENVIADO"));
        Assertions.assertTrue(impresso.contains("Java Basico"));
        Assertions.assertTrue(impresso.contains("DELETADO"));
    }
}
