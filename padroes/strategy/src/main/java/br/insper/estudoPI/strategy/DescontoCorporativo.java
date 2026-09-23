package br.insper.estudoPI.strategy;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component("CORPORATIVO")
public class DescontoCorporativo implements CalculadoraDesconto {

    @Override
    public BigDecimal aplicar(BigDecimal preco) {
        return preco.multiply(new BigDecimal("0.80")).setScale(2, RoundingMode.HALF_UP);
    }
}
