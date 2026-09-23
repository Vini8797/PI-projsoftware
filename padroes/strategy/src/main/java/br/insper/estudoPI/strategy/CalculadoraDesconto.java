package br.insper.estudoPI.strategy;

import java.math.BigDecimal;

// STRATEGY: o "contrato" comum. Cada tipo de desconto é uma classe que implementa isto.
// O service só conhece esta interface, nunca as classes concretas.
public interface CalculadoraDesconto {
    BigDecimal aplicar(BigDecimal preco);
}
