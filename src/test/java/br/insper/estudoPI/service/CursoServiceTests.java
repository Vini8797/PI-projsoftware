package br.insper.estudoPI.service;

import br.insper.estudoPI.dto.CursoDto;
import br.insper.estudoPI.entity.Curso;
import br.insper.estudoPI.exception.CursoNaoEncontradoException;
import br.insper.estudoPI.repository.CursoRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/*
 * TESTE UNITÁRIO: não sobe Spring, não sobe banco, não sobe Docker.
 *   @Mock        -> cria um CursoRepository FALSO
 *   @InjectMocks -> cria o CursoService DE VERDADE e coloca o mock dentro dele
 *
 * Receita de todo teste:
 *   1. monta os dados
 *   2. Mockito.when(mock.metodo(...)).thenReturn(...)   -> ensina o mock
 *   3. chama o método do service
 *   4. Assertions / Mockito.verify                       -> confere
 *
 * Para 100%: cada "if" precisa de um teste que entre e um que NÃO entre.
 * Confira em tests/index.html depois de rodar: .\mvnw.cmd clean test
 */
@ExtendWith(MockitoExtension.class)
public class CursoServiceTests {

    @InjectMocks
    private CursoService cursoService;

    @Mock
    private CursoRepository cursoRepository;

    // TODO [PADRAO] @Mock dos padrões entram AQUI (ver padroes/PADROES.md)

    // ---------------- listar ----------------
    // O "if (nome == null || nome.isBlank())" tem 4 saídas -> 3 testes

    @Test
    public void test_shouldReturnAllCursosWhenFiltroIsNull() {
        List<Curso> cursos = new ArrayList<>();
        cursos.add(new Curso());
        cursos.add(new Curso());

        Mockito.when(cursoRepository.findByDeletadoFalse()).thenReturn(cursos);

        List<Curso> response = cursoService.listar(null);

        Assertions.assertEquals(2, response.size());
        Mockito.verify(cursoRepository).findByDeletadoFalse();
    }

    @Test
    public void test_shouldReturnAllCursosWhenFiltroIsBlank() {
        Mockito.when(cursoRepository.findByDeletadoFalse()).thenReturn(new ArrayList<>());

        List<Curso> response = cursoService.listar("   ");

        Assertions.assertEquals(0, response.size());
        Mockito.verify(cursoRepository).findByDeletadoFalse();
    }

    @Test
    public void test_shouldReturnFilteredCursosWhenFiltroIsNotBlank() {
        Curso curso = new Curso();
        curso.setNome("Java Basico");

        List<Curso> cursos = new ArrayList<>();
        cursos.add(curso);

        Mockito.when(cursoRepository.findByNomeStartingWithIgnoreCaseAndDeletadoFalse("Java"))
                .thenReturn(cursos);

        List<Curso> response = cursoService.listar("Java");

        Assertions.assertEquals(1, response.size());
        Assertions.assertEquals("Java Basico", response.get(0).getNome());
    }

    // ---------------- criar ----------------

    @Test
    public void test_shouldCreateCursoWhenDtoIsValid() {
        CursoDto dto = new CursoDto();
        dto.setNome("Java Basico");
        dto.setDescricao("Introducao a Java");
        dto.setCategoria("Programacao");
        dto.setCargaHoraria(40);
        dto.setPreco(new BigDecimal("199.90"));
        dto.setInstrutor("Eduardo");

        Curso curso = Curso.fromDto(dto);
        curso.setId(1L);

        Mockito.when(cursoRepository.save(Mockito.any(Curso.class))).thenReturn(curso);

        Curso response = cursoService.criar(dto);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(1L, response.getId());
        Assertions.assertEquals("Java Basico", response.getNome());
        Assertions.assertEquals(40, response.getCargaHoraria());
        Assertions.assertFalse(response.isDeletado());
        Assertions.assertNotNull(response.getDataCriacao());
        // TODO [PADRAO] Observer: Mockito.verify(observer).atualizar(curso, "CRIADO");
    }

    // ---------------- deletar ----------------

    @Test
    public void test_shouldSetDeletadoTrueWhenCursoExists() {
        Curso curso = new Curso();
        curso.setId(1L);
        curso.setNome("Java Basico");
        curso.setDeletado(false);

        Mockito.when(cursoRepository.findById(1L)).thenReturn(Optional.of(curso));

        cursoService.deletar(1L);

        // ArgumentCaptor "pega" o objeto que foi passado pro save, pra gente inspecionar
        ArgumentCaptor<Curso> captor = ArgumentCaptor.forClass(Curso.class);
        Mockito.verify(cursoRepository).save(captor.capture());

        Assertions.assertTrue(captor.getValue().isDeletado());
        // prova que NÃO foi deleção física
        Mockito.verify(cursoRepository, Mockito.never()).deleteById(Mockito.anyLong());
        // TODO [PADRAO] Observer: Mockito.verify(observer).atualizar(curso, "DELETADO");
    }

    @Test
    public void test_shouldThrowExceptionWhenCursoDoesNotExist() {
        Mockito.when(cursoRepository.findById(99L)).thenReturn(Optional.empty());

        CursoNaoEncontradoException exception = Assertions.assertThrows(
                CursoNaoEncontradoException.class,
                () -> cursoService.deletar(99L)
        );

        Assertions.assertEquals("Curso com ID 99 não encontrado", exception.getMessage());
        Mockito.verify(cursoRepository, Mockito.never()).save(Mockito.any());
    }

    // TODO [PROVA] Testes dos novos métodos do service entram AQUI (um por caminho do if)

    // TODO [PADRAO] Testes dos padrões entram AQUI
}
