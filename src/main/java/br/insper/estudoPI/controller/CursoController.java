package br.insper.estudoPI.controller;

import br.insper.estudoPI.dto.CursoDto;
import br.insper.estudoPI.entity.Curso;
import br.insper.estudoPI.service.CursoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//teste do pipeline
// O controller NÃO tem regra de negócio: só recebe a requisição e repassa pro service.
// TODO [PROVA] Troque "/cursos" pela rota do enunciado
@RestController
@RequestMapping("/cursos")
public class CursoController {

    @Autowired
    private CursoService cursoService;

    // required = false -> GET /cursos funciona sem o ?nome=
    @GetMapping
    public List<Curso> listar(@RequestParam(required = false) String nome) {
        return cursoService.listar(nome);
    }

    // @RequestBody -> converte o JSON do corpo em CursoDto
    // Se o enunciado pedir 201: adicione @ResponseStatus(HttpStatus.CREATED) e troque isOk() por isCreated() no teste
    @PostMapping
    public Curso criar(@RequestBody CursoDto dto) {
        return cursoService.criar(dto);
    }

    // @PathVariable -> lê o {id} da URL
    // Se o enunciado pedir 204: adicione @ResponseStatus(HttpStatus.NO_CONTENT) e troque isOk() por isNoContent() no teste
    @DeleteMapping("/{id}")
    public void deletar(@PathVariable Long id) {
        cursoService.deletar(id);
    }

    // TODO [PROVA] Novas rotas do enunciado entram AQUI

    // TODO [PADRAO] Rotas dos padrões entram AQUI (/{id}/preco, /{id}/certificado)
}
