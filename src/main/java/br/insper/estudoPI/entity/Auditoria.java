package br.insper.estudoPI.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "auditoria")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false)
    private String tipoOperacao;

    @Column
    private Long avaliacaoId;

    public static Auditoria de(Long avaliacaoId, String tipoOperacao) {
        Auditoria auditoria = new Auditoria();
        auditoria.setTimestamp(LocalDateTime.now());
        auditoria.setTipoOperacao(tipoOperacao);
        auditoria.setAvaliacaoId(avaliacaoId);
        return auditoria;
    }
}