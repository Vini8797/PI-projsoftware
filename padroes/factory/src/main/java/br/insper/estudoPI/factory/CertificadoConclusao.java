package br.insper.estudoPI.factory;

// Não é @Component: cada certificado tem DADOS próprios (aluno, curso),
// então precisa de um objeto novo a cada pedido. Quem cria é a factory, com "new".
public class CertificadoConclusao implements Certificado {

    private final String aluno;
    private final String curso;
    private final Integer cargaHoraria;

    public CertificadoConclusao(String aluno, String curso, Integer cargaHoraria) {
        this.aluno = aluno;
        this.curso = curso;
        this.cargaHoraria = cargaHoraria;
    }

    @Override
    public String gerarTexto() {
        return "Certificamos que " + aluno + " concluiu o curso " + curso
                + " com carga horaria de " + cargaHoraria + " horas.";
    }
}
