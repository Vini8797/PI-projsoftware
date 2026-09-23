package br.insper.estudoPI.observer;

import br.insper.estudoPI.entity.Curso;
import org.springframework.stereotype.Component;

@Component
public class EmailObserver implements CursoObserver {

    @Override
    public void atualizar(Curso curso, String evento) {
        System.out.println("EMAIL ENVIADO - O curso " + curso.getNome() + " foi " + evento);
    }
}
