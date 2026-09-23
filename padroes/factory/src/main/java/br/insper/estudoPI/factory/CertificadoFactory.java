package br.insper.estudoPI.factory;

import br.insper.estudoPI.entity.Curso;
import org.springframework.stereotype.Component;

// FACTORY: o ÚNICO lugar do sistema que decide qual classe criar.
// Todos os "new CertificadoXxx" do projeto ficam aqui, e em mais nenhum lugar.
// É @Component (e não método static) para poder ser MOCKADA no teste do service.
@Component
public class CertificadoFactory {

    public Certificado criar(TipoCertificado tipo, String aluno, Curso curso) {
        return switch (tipo) {
            case CONCLUSAO -> new CertificadoConclusao(aluno, curso.getNome(), curso.getCargaHoraria());
            case PARTICIPACAO -> new CertificadoParticipacao(aluno, curso.getNome());
            case EXCELENCIA -> new CertificadoExcelencia(aluno, curso.getNome(), curso.getCargaHoraria());
        };
    }
}
