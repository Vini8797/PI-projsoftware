package br.insper.estudoPI.factory;

// FACTORY: o "produto". Quem pede um certificado recebe esta interface
// e não sabe (nem precisa saber) qual classe concreta veio.
public interface Certificado {
    String gerarTexto();
}
