package br.insper.estudoPI.controller;

import br.insper.estudoPI.dto.CursoDto;
import br.insper.estudoPI.entity.Curso;
import br.insper.estudoPI.repository.CursoRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/*
 * TESTE DE INTEGRAÇÃO: sobe a aplicação INTEIRA + um Postgres de verdade num container descartável.
 * Precisa do Docker Desktop ligado.
 *
 *   @SpringBootTest          -> sobe o Spring todo
 *   @AutoConfigureMockMvc    -> dá o MockMvc, que simula requisições HTTP
 *   @Testcontainers          -> sobe/derruba o container junto com a classe
 *   @DynamicPropertySource   -> o container sorteia uma porta; este método troca a URL/usuário/senha
 *                               do application.properties pelos do container, ANTES do Spring conectar
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class CursoControllerTests {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("curso_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CursoRepository cursoRepository;

    // Limpa o banco antes de CADA teste. Sem isso um teste herda os dados do outro.
    @BeforeEach
    void limparBanco() {
        cursoRepository.deleteAll();
    }

    // TODO [PROVA] Ajuste os campos aqui se a entidade mudar
    private CursoDto criarDto(String nome) {
        CursoDto dto = new CursoDto();
        dto.setNome(nome);
        dto.setDescricao("Descricao de " + nome);
        dto.setCategoria("Programacao");
        dto.setCargaHoraria(40);
        dto.setPreco(new BigDecimal("200.00"));
        dto.setInstrutor("Eduardo");
        return dto;
    }

    // ---------------- POST /cursos ----------------

    @Test
    public void test_shouldCreateCursoWhenPostIsCalled() throws Exception {
        CursoDto dto = criarDto("Java Basico");

        mockMvc.perform(post("/cursos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.nome").value("Java Basico"))
                .andExpect(jsonPath("$.cargaHoraria").value(40))
                .andExpect(jsonPath("$.deletado").value(false));
    }

    // ---------------- GET /cursos ----------------

    @Test
    public void test_shouldReturnOnlyCursosStartingWithFiltro() throws Exception {
        cursoRepository.save(Curso.fromDto(criarDto("Java Basico")));
        cursoRepository.save(Curso.fromDto(criarDto("Java Avancado")));
        cursoRepository.save(Curso.fromDto(criarDto("Python para Dados")));

        // sem filtro: os 3
        mockMvc.perform(get("/cursos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));

        // startWith: só os 2 de Java
        mockMvc.perform(get("/cursos").param("nome", "Java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        // "Basico" está no MEIO do nome -> não pode casar com startWith
        mockMvc.perform(get("/cursos").param("nome", "Basico"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        // ignora maiúscula/minúscula
        mockMvc.perform(get("/cursos").param("nome", "java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    // ---------------- DELETE /cursos/{id} ----------------

    @Test
    public void test_shouldDeleteLogicallyWhenDeleteIsCalled() throws Exception {
        Curso curso = cursoRepository.save(Curso.fromDto(criarDto("Java Basico")));
        cursoRepository.save(Curso.fromDto(criarDto("Python para Dados")));

        mockMvc.perform(delete("/cursos/" + curso.getId()))
                .andExpect(status().isOk());

        // sumiu da listagem...
        mockMvc.perform(get("/cursos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nome").value("Python para Dados"));

        // ...mas continua no banco com deletado = true  (isso PROVA que é lógica)
        Curso doBanco = cursoRepository.findById(curso.getId()).orElseThrow();
        Assertions.assertTrue(doBanco.isDeletado());
    }

    @Test
    public void test_shouldReturn404WhenDeletingCursoThatDoesNotExist() throws Exception {
        mockMvc.perform(delete("/cursos/9999"))
                .andExpect(status().isNotFound());
    }

    // TODO [PROVA] Testes de integração das novas rotas entram AQUI

    // TODO [PADRAO] Testes de integração das rotas dos padrões entram AQUI
}
