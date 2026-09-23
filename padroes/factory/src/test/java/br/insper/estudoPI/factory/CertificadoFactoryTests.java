package br.insper.estudoPI.factory;

import br.insper.estudoPI.entity.Curso;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

// Testa a factory de verdade (sem mock): para cada tipo, confere a CLASSE que saiu e o texto.
public class CertificadoFactoryTests {

    private final CertificadoFactory factory = new CertificadoFactory();

    private Curso criarCurso() {
        Curso curso = new Curso();
        curso.setNome("Java Basico");
        curso.setCargaHoraria(40);
        return curso;
    }

    @Test
    public void test_shouldCreateCertificadoConclusao() {
        Certificado certificado = factory.criar(TipoCertificado.CONCLUSAO, "Maria", criarCurso());

        Assertions.assertInstanceOf(CertificadoConclusao.class, certificado);
        Assertions.assertTrue(certificado.gerarTexto().contains("Maria"));
        Assertions.assertTrue(certificado.gerarTexto().contains("concluiu"));
    }

    @Test
    public void test_shouldCreateCertificadoParticipacao() {
        Certificado certificado = factory.criar(TipoCertificado.PARTICIPACAO, "Maria", criarCurso());

        Assertions.assertInstanceOf(CertificadoParticipacao.class, certificado);
        Assertions.assertTrue(certificado.gerarTexto().contains("participou"));
    }

    @Test
    public void test_shouldCreateCertificadoExcelencia() {
        Certificado certificado = factory.criar(TipoCertificado.EXCELENCIA, "Maria", criarCurso());

        Assertions.assertInstanceOf(CertificadoExcelencia.class, certificado);
        Assertions.assertTrue(certificado.gerarTexto().contains("EXCELENCIA"));
    }
}
