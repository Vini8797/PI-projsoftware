package br.insper.estudoPI.factory;

public class CertificadoExcelencia implements Certificado {

    private final String aluno;
    private final String curso;
    private final Integer cargaHoraria;

    public CertificadoExcelencia(String aluno, String curso, Integer cargaHoraria) {
        this.aluno = aluno;
        this.curso = curso;
        this.cargaHoraria = cargaHoraria;
    }

    @Override
    public String gerarTexto() {
        return "Certificamos que " + aluno + " concluiu com EXCELENCIA o curso " + curso
                + " (" + cargaHoraria + " horas).";
    }
}
