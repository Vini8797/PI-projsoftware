package br.insper.estudoPI.strategy;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

// O texto dentro de @Component vira a CHAVE no Map que o Spring injeta no service.
// Map<String, CalculadoraDesconto> -> { "ESTUDANTE": DescontoEstudante, "CORPORATIVO": ..., "NENHUM": ... }
@Component("ESTUDANTE")
public class DescontoEstudante implements CalculadoraDesconto {

    @Override
    public BigDecimal aplicar(BigDecimal preco) {
        return preco.multiply(new BigDecimal("0.50")).setScale(2, RoundingMode.HALF_UP);
    }
}
