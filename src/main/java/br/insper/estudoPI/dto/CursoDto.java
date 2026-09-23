package br.insper.estudoPI.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

// O DTO é o que o cliente manda no POST.
// Não tem id, dataCriacao nem deletado: o cliente não pode escolher esses campos.
// TODO [PROVA] Espelhe aqui os campos da entidade que o cliente pode preencher
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CursoDto {
    private String nome;
    private String descricao;
    private String categoria;
    private Integer cargaHoraria;
    private BigDecimal preco;
    private String instrutor;
}
