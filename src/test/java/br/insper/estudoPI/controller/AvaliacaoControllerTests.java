package br.insper.estudoPI.controller;

import br.insper.estudoPI.dto.AvaliacaoDto;
import br.insper.estudoPI.entity.Auditoria;
import br.insper.estudoPI.entity.Avaliacao;
import br.insper.estudoPI.repository.AuditoriaRepository;
import br.insper.estudoPI.repository.AvaliacaoRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class AvaliacaoControllerTests {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("avaliacao_test")
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
    private AvaliacaoRepository avaliacaoRepository;

    @Autowired
    private AuditoriaRepository auditoriaRepository;

    @BeforeEach
    void limparBanco() {
        avaliacaoRepository.deleteAll();
        auditoriaRepository.deleteAll();
    }

    private AvaliacaoDto criarDto(String autor) {
        return criarDto(autor, 5);
    }

    private AvaliacaoDto criarDto(String autor, Integer nota) {
        AvaliacaoDto dto = new AvaliacaoDto();
        dto.setAutor(autor);
        dto.setConteudo("Comentario de " + autor);
        dto.setNota(nota);
        return dto;
    }

    @Test
    public void test_shouldCreateAvaliacaoWhenPostIsCalled() throws Exception {
        AvaliacaoDto dto = criarDto("James Bond");

        mockMvc.perform(post("/avaliacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.autor").value("James Bond"))
                .andExpect(jsonPath("$.nota").value(5))
                .andExpect(jsonPath("$.dataAvaliacao").exists())
                .andExpect(jsonPath("$.deletado").value(false));
    }

    @Test
    public void test_shouldReturnAvaliacaoWhenGetByIdIsCalled() throws Exception {
        Avaliacao avaliacao = avaliacaoRepository.save(Avaliacao.fromDto(criarDto("James Bond")));

        mockMvc.perform(get("/avaliacao/" + avaliacao.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(avaliacao.getId()))
                .andExpect(jsonPath("$.autor").value("James Bond"))
                .andExpect(jsonPath("$.conteudo").value("Comentario de James Bond"))
                .andExpect(jsonPath("$.nota").value(5));
    }

    @Test
    public void test_shouldReturn404WhenGetByIdDoesNotExist() throws Exception {
        mockMvc.perform(get("/avaliacao/9999"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void test_shouldReturnOnlyAvaliacaoStartingWithFiltro() throws Exception {
        avaliacaoRepository.save(Avaliacao.fromDto(criarDto("James Bond")));
        avaliacaoRepository.save(Avaliacao.fromDto(criarDto("James Silva")));
        avaliacaoRepository.save(Avaliacao.fromDto(criarDto("alice Souza")));

        // sem filtro: os 3
        mockMvc.perform(get("/avaliacao"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));

        // startWith: só os 2 do James
        mockMvc.perform(get("/avaliacao").param("autor", "James"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        // "Bond" está no MEIO do nome -> não pode casar com startWith
        mockMvc.perform(get("/avaliacao").param("autor", "Bond"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        // ignora maiúscula/minúscula
        mockMvc.perform(get("/avaliacao").param("autor", "ALICE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    public void test_shouldDeleteLogicallyWhenDeleteIsCalled() throws Exception {
        Avaliacao avaliacao = avaliacaoRepository.save(Avaliacao.fromDto(criarDto("James Bond")));
        avaliacaoRepository.save(Avaliacao.fromDto(criarDto("Jacinto")));

        mockMvc.perform(delete("/avaliacao/" + avaliacao.getId()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/avaliacao"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].autor").value("Jacinto"));

        Avaliacao doBanco = avaliacaoRepository.findById(avaliacao.getId()).orElseThrow();
        Assertions.assertTrue(doBanco.isDeletado());
    }

    @Test
    public void test_shouldReturn404WhenDeletingAvaliacaoThatDoesNotExist() throws Exception {
        mockMvc.perform(delete("/avaliacao/9999"))
                .andExpect(status().isNotFound());
    }

    // ===== Observable 1: auditoria gravada no banco a cada CREATE e DELETE =====

    @Test
    public void test_shouldRegisterAuditoriaOnCreateAndDelete() throws Exception {
        String resposta = mockMvc.perform(post("/avaliacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(criarDto("James Bond"))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(resposta).get("id").asLong();

        mockMvc.perform(delete("/avaliacao/" + id))
                .andExpect(status().isOk());

        // ordena por id: a ordem do findAll() sem Sort não é garantida
        List<Auditoria> registros = auditoriaRepository.findAll(Sort.by("id"));

        Assertions.assertEquals(2, registros.size());
        Assertions.assertEquals("CRIADO", registros.get(0).getTipoOperacao());
        Assertions.assertEquals("DELETADO", registros.get(1).getTipoOperacao());
        Assertions.assertNotNull(registros.get(0).getTimestamp());
        Assertions.assertNotNull(registros.get(1).getTimestamp());
        Assertions.assertEquals(id, registros.get(0).getAvaliacaoId());
    }

    // ===== Observable 2: moderação dispara no log para nota 1 ou 2 (ver console do teste) =====

    @Test
    public void test_shouldCreateAvaliacaoNegativaAndTriggerModeracao() throws Exception {
        mockMvc.perform(post("/avaliacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(criarDto("James Bond", 1))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nota").value(1));

        Assertions.assertEquals(1, auditoriaRepository.findAll().size());
    }
}
