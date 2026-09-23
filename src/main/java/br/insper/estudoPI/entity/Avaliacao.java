package br.insper.estudoPI.entity;

import br.insper.estudoPI.dto.AvaliacaoDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "avaliacao")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Avaliacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String autor;

    @Column
    private String conteudo;

    @Column
    private Integer nota;

    @Column
    private LocalDate dataAvaliacao;

    @Column(nullable = false)
    private boolean deletado;

    public static Avaliacao fromDto(AvaliacaoDto dto) {
        Avaliacao avaliacao = new Avaliacao();
        avaliacao.setAutor(dto.getAutor());
        avaliacao.setConteudo(dto.getConteudo());
        avaliacao.setNota(dto.getNota());
        avaliacao.setDataAvaliacao(LocalDate.now());
        avaliacao.setDeletado(false);
        return avaliacao;
    }
}
