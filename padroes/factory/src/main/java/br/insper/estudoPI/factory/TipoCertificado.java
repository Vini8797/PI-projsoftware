package br.insper.estudoPI.factory;

// Na URL: ?tipo=CONCLUSAO -> o Spring converte a String no enum sozinho (valor inválido = 400)
public enum TipoCertificado {
    CONCLUSAO,
    PARTICIPACAO,
    EXCELENCIA
}
