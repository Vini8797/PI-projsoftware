package br.insper.estudoPI.strategy;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// Tipo de desconto que não existe no Map -> 400 Bad Request
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class DescontoInvalidoException extends RuntimeException {
    public DescontoInvalidoException(String mensagem) {
        super(mensagem);
    }
}
