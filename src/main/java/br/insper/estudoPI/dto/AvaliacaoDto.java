package br.insper.estudoPI.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AvaliacaoDto {
    private String autor;
    private String conteudo;
    private Integer nota;
    private LocalDate dataAvaliacao;
}
