package br.insper.estudoPI.controller;

import br.insper.estudoPI.dto.AvaliacaoDto;
import br.insper.estudoPI.entity.Avaliacao;
import br.insper.estudoPI.service.AvaliacaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/avaliacao")
public class AvaliacaoController {

    @Autowired
    private AvaliacaoService avaliacaoService;

    @GetMapping
    public List<Avaliacao> listar(@RequestParam(required = false) String autor) {
        return avaliacaoService.listar(autor);
    }

    // Id inexistente -> AvaliacaoNaoEncontradaException -> 404 (via @ResponseStatus)
    @GetMapping("/{id}")
    public Avaliacao obterPorId(@PathVariable Long id) {
        return avaliacaoService.obterPorId(id);
    }

    @PostMapping
    public Avaliacao criar(@RequestBody AvaliacaoDto dto) {
        return avaliacaoService.criar(dto);
    }

    @DeleteMapping("/{id}")
    public void deletar(@PathVariable Long id) {
        avaliacaoService.deletar(id);
    }
}
