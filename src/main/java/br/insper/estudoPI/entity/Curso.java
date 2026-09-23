package br.insper.estudoPI.entity;

import br.insper.estudoPI.dto.CursoDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

// TODO [PROVA] Se o domínio mudar (ex: Livro), renomeie a classe com Shift+F6 e troque o nome da tabela
@Entity
@Table(name = "cursos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Curso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // TODO [PROVA] Campos do enunciado. Se adicionar/remover, atualize também o CursoDto e o fromDto()
    @Column(nullable = false)
    private String nome;

    @Column
    private String descricao;

    @Column
    private String categoria;

    @Column
    private Integer cargaHoraria;

    @Column
    private BigDecimal preco;

    @Column
    private String instrutor;

    @Column
    private LocalDate dataCriacao;

    // Deleção lógica. boolean primitivo (nunca fica NULL no banco).
    // O Lombok gera isDeletado() e setDeletado(), não getDeletado().
    @Column(nullable = false)
    private boolean deletado;

    // Factory method estático: converte DTO -> Entity num lugar só
    public static Curso fromDto(CursoDto dto) {
        Curso curso = new Curso();
        curso.setNome(dto.getNome());
        curso.setDescricao(dto.getDescricao());
        curso.setCategoria(dto.getCategoria());
        curso.setCargaHoraria(dto.getCargaHoraria());
        curso.setPreco(dto.getPreco());
        curso.setInstrutor(dto.getInstrutor());
        // campos que o CLIENTE não escolhe: o sistema define
        curso.setDataCriacao(LocalDate.now());
        curso.setDeletado(false);
        return curso;
    }
}
