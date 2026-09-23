package br.insper.estudoPI.strategy;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

// Cada estratégia é uma classe simples: testa com "new", sem mock nenhum.
public class CalculadoraDescontoTests {

    @Test
    public void test_estudanteDeveDarCinquentaPorCento() {
        BigDecimal resultado = new DescontoEstudante().aplicar(new BigDecimal("200.00"));
        Assertions.assertEquals(new BigDecimal("100.00"), resultado);
    }

    @Test
    public void test_corporativoDeveDarVintePorCento() {
        BigDecimal resultado = new DescontoCorporativo().aplicar(new BigDecimal("200.00"));
        Assertions.assertEquals(new BigDecimal("160.00"), resultado);
    }

    @Test
    public void test_semDescontoDeveManterPreco() {
        BigDecimal resultado = new SemDesconto().aplicar(new BigDecimal("200.00"));
        Assertions.assertEquals(new BigDecimal("200.00"), resultado);
    }
}
