package br.insper.estudoPI.strategy;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component("NENHUM")
public class SemDesconto implements CalculadoraDesconto {

    @Override
    public BigDecimal aplicar(BigDecimal preco) {
        return preco.setScale(2, RoundingMode.HALF_UP);
    }
}
