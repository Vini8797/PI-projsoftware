package br.insper.estudoPI.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// @ResponseStatus: quando essa exception escapar do controller, o Spring responde 404 (e não 500).
// RuntimeException: não obriga ninguém a declarar "throws".
@ResponseStatus(HttpStatus.NOT_FOUND)
public class CursoNaoEncontradoException extends RuntimeException {
    public CursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
