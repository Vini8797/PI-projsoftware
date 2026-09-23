package br.insper.estudoPI.factory;

public class CertificadoParticipacao implements Certificado {

    private final String aluno;
    private final String curso;

    public CertificadoParticipacao(String aluno, String curso) {
        this.aluno = aluno;
        this.curso = curso;
    }

    @Override
    public String gerarTexto() {
        return "Certificamos que " + aluno + " participou do curso " + curso + ".";
    }
}
