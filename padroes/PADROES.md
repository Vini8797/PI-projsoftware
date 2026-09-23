# Padrões de Projeto — Strategy, Factory e Observer

Esta pasta **não é compilada** (está fora de `src/`). Ela não interfere em nada do projeto.

- **Se cair um padrão:** copie a pasta dele para dentro de `src/` e cole os trechos indicados.
- **Se não cair nenhum:** apague tudo com `Remove-Item -Recurse -Force padroes`.

Cada padrão é independente. Pode usar um, dois ou os três.

---

## Como reconhecer no enunciado

| Se o enunciado fala algo como... | Padrão |
|---|---|
| "dependendo do **tipo**, **calcular / processar / validar** de forma diferente" | **Strategy** |
| "o preço muda conforme o plano", "cada forma de pagamento é processada de um jeito" | **Strategy** |
| "**criar** objetos diferentes conforme o tipo", "**centralizar a criação**" | **Factory** |
| "gerar um documento/notificação/relatório do tipo X, Y ou Z" | **Factory** |
| "**quando** algo acontecer, **notificar / registrar log / enviar e-mail**" | **Observer** |
| "avisar vários componentes quando o status mudar" | **Observer** |

## Strategy × Factory — a confusão clássica

Os dois têm "um tipo" e "várias classes". A diferença está na **pergunta que cada um responde**:

| | Factory | Strategy |
|---|---|---|
| Pergunta | "Qual objeto eu **crio**?" | "Qual algoritmo eu **uso**?" |
| O que resolve (slide do professor) | criação de objetos **espalhada** pelo código | `if/else` que **cresce** a cada comportamento novo |
| Os objetos | **novos a cada pedido**, carregam dados (aluno, curso) | **já existem**, não guardam dados, só comportamento |
| No Spring | classe `@Component` com `switch` + `new` | várias classes `@Component("NOME")` + `Map<String, Interface>` |
| Palavra-chave | `new` | `implements` + escolher no `Map` |

---

## Copiando um padrão para o projeto

Na raiz do projeto (PowerShell):

```powershell
robocopy padroes\strategy\src src /E    # Strategy
robocopy padroes\factory\src src /E     # Factory
robocopy padroes\observer\src src /E    # Observer
```

O `robocopy` junta as pastas sem apagar nada. Ele mostra uma tabela no final — **é normal**, não é erro.

Sem terminal: abra `padroes/strategy/src/main/java/br/insper/estudoPI/`, copie a pasta `strategy` e cole dentro de `src/main/java/br/insper/estudoPI/`. Faça o mesmo com a pasta de teste.

Depois de copiar, siga os passos do padrão abaixo. Todos os pontos de encaixe estão marcados com `TODO [PADRAO]` no código — no IntelliJ, **Alt+6** abre a janela de TODOs e lista todos.

---

# 1. Strategy

## A ideia

Pense num GPS. Você pede "me leve até o Insper" e ele pode calcular a rota **de carro, a pé ou de bicicleta**. O pedido é o mesmo; o **algoritmo** muda.

**Sem o padrão**, o service vira isso, e cresce a cada tipo novo:

```java
if (tipo.equals("ESTUDANTE")) {
    return preco * 0.5;
} else if (tipo.equals("CORPORATIVO")) {
    return preco * 0.8;
} else if (tipo.equals("BLACK_FRIDAY")) {   // ← toda vez mexe aqui
    ...
}
```

**Com o padrão**, cada comportamento vira uma classe que implementa a mesma interface. O service só pergunta "qual estratégia eu uso?" e chama.

```
                  «interface»
              CalculadoraDesconto
              + aplicar(preco)
                      ▲
       ┌──────────────┼──────────────┐
DescontoEstudante  DescontoCorporativo  SemDesconto
  @Component         @Component         @Component
  ("ESTUDANTE")      ("CORPORATIVO")    ("NENHUM")
```

**Frase para explicar na prova:** *"Cada forma de calcular o desconto é uma classe separada que implementa a mesma interface. Para adicionar um desconto novo, eu crio uma classe nova e não mexo no service — isso é o princípio Aberto/Fechado. E cada estratégia é testada isoladamente."*

## O truque do Spring: o `Map`

Quando você escreve no service:

```java
@Autowired
private Map<String, CalculadoraDesconto> calculadoras;
```

o Spring procura **todas** as classes que implementam `CalculadoraDesconto` e monta o mapa sozinho, usando o texto do `@Component` como chave:

```
{ "ESTUDANTE" → DescontoEstudante, "CORPORATIVO" → DescontoCorporativo, "NENHUM" → SemDesconto }
```

É exatamente o que o professor fez na API de pagamento com `Map<String, Processador>` e `@Service("PIX")`, `@Service("BOLETO")`.

## Arquivos (já prontos na pasta)

| Arquivo | Papel |
|---|---|
| `CalculadoraDesconto.java` | a interface (o contrato) |
| `DescontoEstudante.java` | 50% de desconto, chave `"ESTUDANTE"` |
| `DescontoCorporativo.java` | 20% de desconto, chave `"CORPORATIVO"` |
| `SemDesconto.java` | preço cheio, chave `"NENHUM"` |
| `DescontoInvalidoException.java` | tipo inexistente → 400 |
| `CalculadoraDescontoTests.java` | testa cada estratégia com `new` |

## Passo 1 — Copiar

```powershell
robocopy padroes\strategy\src src /E
```

## Passo 2 — `CursoService`

Imports novos:

```java
import br.insper.estudoPI.strategy.CalculadoraDesconto;
import br.insper.estudoPI.strategy.DescontoInvalidoException;

import java.math.BigDecimal;
import java.util.Map;
```

No `TODO [PADRAO] Campos @Autowired`:

```java
    @Autowired
    private Map<String, CalculadoraDesconto> calculadoras;
```

No `TODO [PADRAO] Métodos dos padrões`:

```java
    // GET /cursos/{id}/preco?desconto=ESTUDANTE
    public BigDecimal calcularPrecoFinal(Long id, String tipoDesconto) {
        Curso curso = buscarCurso(id);
        CalculadoraDesconto calculadora = calculadoras.get(tipoDesconto.toUpperCase());
        if (calculadora == null) {
            throw new DescontoInvalidoException("Tipo de desconto invalido: " + tipoDesconto);
        }
        return calculadora.aplicar(curso.getPreco());
    }
```

Repare: **não tem nenhum `if` por tipo de desconto**. O único `if` é para o caso de o tipo não existir.

## Passo 3 — `CursoController`

Import novo: `import java.math.BigDecimal;`

No `TODO [PADRAO] Rotas dos padrões`:

```java
    // defaultValue -> se não mandar ?desconto=, usa NENHUM
    @GetMapping("/{id}/preco")
    public BigDecimal calcularPreco(@PathVariable Long id,
                                    @RequestParam(defaultValue = "NENHUM") String desconto) {
        return cursoService.calcularPrecoFinal(id, desconto);
    }
```

## Passo 4 — Testes unitários (`CursoServiceTests`)

Imports novos:

```java
import br.insper.estudoPI.strategy.CalculadoraDesconto;
import br.insper.estudoPI.strategy.DescontoInvalidoException;

import java.util.Map;
```

No `TODO [PADRAO] @Mock`:

```java
    @Mock
    private Map<String, CalculadoraDesconto> calculadoras;   // o Map inteiro é falso

    @Mock
    private CalculadoraDesconto calculadora;                  // a estratégia que o Map vai devolver
```

No `TODO [PADRAO] Testes dos padrões` — **2 testes**, um para cada lado do `if`:

```java
    @Test
    public void test_shouldApplyDescontoWhenTipoIsValid() {
        Curso curso = new Curso();
        curso.setId(1L);
        curso.setPreco(new BigDecimal("200.00"));

        Mockito.when(cursoRepository.findById(1L)).thenReturn(Optional.of(curso));
        Mockito.when(calculadoras.get("ESTUDANTE")).thenReturn(calculadora);
        Mockito.when(calculadora.aplicar(curso.getPreco())).thenReturn(new BigDecimal("100.00"));

        // minúsculo de propósito: prova que o toUpperCase() funciona
        BigDecimal resultado = cursoService.calcularPrecoFinal(1L, "estudante");

        Assertions.assertEquals(new BigDecimal("100.00"), resultado);
    }

    @Test
    public void test_shouldThrowWhenTipoDescontoIsInvalid() {
        Curso curso = new Curso();
        curso.setId(1L);
        curso.setPreco(new BigDecimal("200.00"));

        Mockito.when(cursoRepository.findById(1L)).thenReturn(Optional.of(curso));
        Mockito.when(calculadoras.get("INEXISTENTE")).thenReturn(null);

        Assertions.assertThrows(DescontoInvalidoException.class,
                () -> cursoService.calcularPrecoFinal(1L, "INEXISTENTE"));
    }
```

**Por que mockar a estratégia no teste do service?** O teste do service quer provar que ele **escolhe e chama** a estratégia certa — não que a conta de 50% está certa. A conta é testada separadamente em `CalculadoraDescontoTests`. Cada teste com uma responsabilidade.

## Passo 5 — Teste de integração (`CursoControllerTests`)

No `TODO [PADRAO] Testes de integração`:

```java
    @Test
    public void test_shouldReturnPrecoComDescontoEstudante() throws Exception {
        Curso curso = cursoRepository.save(Curso.fromDto(criarDto("Java Basico")));  // preço 200.00

        mockMvc.perform(get("/cursos/" + curso.getId() + "/preco").param("desconto", "ESTUDANTE"))
                .andExpect(status().isOk())
                .andExpect(content().string("100.00"));
    }

    @Test
    public void test_shouldReturn400WhenDescontoIsInvalid() throws Exception {
        Curso curso = cursoRepository.save(Curso.fromDto(criarDto("Java Basico")));

        mockMvc.perform(get("/cursos/" + curso.getId() + "/preco").param("desconto", "BLACKFRIDAY"))
                .andExpect(status().isBadRequest());
    }
```

## Testando na mão

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/cursos/1/preco?desconto=ESTUDANTE" -Method Get
Invoke-RestMethod -Uri "http://localhost:8080/cursos/1/preco?desconto=CORPORATIVO" -Method Get
Invoke-RestMethod -Uri "http://localhost:8080/cursos/1/preco" -Method Get
```

## Pegadinhas

- **`BigDecimal` compara a escala.** `new BigDecimal("100")` **não é igual** a `new BigDecimal("100.00")` no `assertEquals`. Por isso as estratégias fazem `.setScale(2, RoundingMode.HALF_UP)` e os testes usam sempre duas casas.
- **A chave do `@Component` tem que bater letra por letra** com o que vem na URL. O `toUpperCase()` resolve maiúscula/minúscula.
- **Adicionar um desconto novo:** nova classe + `implements CalculadoraDesconto` + `@Component("NOVO")` + um teste. O service não muda. Essa é a graça.

---

# 2. Factory

## A ideia

Pense numa gráfica. Você chega e diz "quero um certificado de **conclusão** para a Maria". Você não sabe qual máquina a gráfica usa nem como monta — você recebe o certificado pronto. A gráfica é a **fábrica**.

**Sem o padrão**, o `new` fica espalhado pelo código (é o problema do slide do professor):

```java
// no service
if (tipo == CONCLUSAO) cert = new CertificadoConclusao(...);
// no controller
if (tipo == PARTICIPACAO) cert = new CertificadoParticipacao(...);
// em outro lugar... de novo
```

**Com o padrão**, um único lugar decide qual classe criar:

```
    CursoService ──pede──▶ CertificadoFactory.criar(tipo, aluno, curso)
                                     │  switch + new
                   ┌─────────────────┼─────────────────┐
                   ▼                 ▼                 ▼
         CertificadoConclusao  CertificadoParticipacao  CertificadoExcelencia
                   └──────── todos implementam «Certificado» ────────┘
```

**Frase para explicar na prova:** *"A factory centraliza a criação dos objetos. O service pede um certificado de um tipo e recebe a interface, sem saber qual classe concreta veio. Se surgir um tipo novo, eu mudo só a factory."*

**Você já usa um factory:** o `Curso.fromDto(dto)` é um *factory method* estático — ele centraliza a criação de um `Curso` a partir do DTO.

## Por que aqui é Factory e não Strategy?

Cada certificado **carrega dados** (o nome da Maria, o nome do curso). Precisa de um objeto **novo** a cada pedido. Um bean do Spring (`@Component`) é um só para a aplicação inteira — não daria para guardar a Maria nele. Por isso os certificados **não são** `@Component`, e quem faz `new` é a factory.

## Arquivos (já prontos na pasta)

| Arquivo | Papel |
|---|---|
| `TipoCertificado.java` | enum `CONCLUSAO`, `PARTICIPACAO`, `EXCELENCIA` |
| `Certificado.java` | a interface do produto |
| `CertificadoConclusao.java` / `Participacao` / `Excelencia` | os produtos concretos |
| `CertificadoFactory.java` | o `switch` que decide qual criar |
| `CertificadoFactoryTests.java` | testa cada tipo |

## Passo 1 — Copiar

```powershell
robocopy padroes\factory\src src /E
```

## Passo 2 — `CursoService`

Imports novos:

```java
import br.insper.estudoPI.factory.Certificado;
import br.insper.estudoPI.factory.CertificadoFactory;
import br.insper.estudoPI.factory.TipoCertificado;
```

No `TODO [PADRAO] Campos @Autowired`:

```java
    @Autowired
    private CertificadoFactory certificadoFactory;
```

No `TODO [PADRAO] Métodos dos padrões`:

```java
    // GET /cursos/{id}/certificado?tipo=CONCLUSAO&aluno=Maria
    public String gerarCertificado(Long id, TipoCertificado tipo, String aluno) {
        Curso curso = buscarCurso(id);
        Certificado certificado = certificadoFactory.criar(tipo, aluno, curso);
        return certificado.gerarTexto();
    }
```

Sem `if` nenhum — o `switch` mora na factory. O service fica com 100% com um teste só.

## Passo 3 — `CursoController`

Import novo: `import br.insper.estudoPI.factory.TipoCertificado;`

No `TODO [PADRAO] Rotas dos padrões`:

```java
    // O Spring converte ?tipo=CONCLUSAO no enum sozinho. Valor inválido -> 400 automático.
    @GetMapping("/{id}/certificado")
    public String gerarCertificado(@PathVariable Long id,
                                   @RequestParam TipoCertificado tipo,
                                   @RequestParam String aluno) {
        return cursoService.gerarCertificado(id, tipo, aluno);
    }
```

## Passo 4 — Testes unitários (`CursoServiceTests`)

Imports novos:

```java
import br.insper.estudoPI.factory.Certificado;
import br.insper.estudoPI.factory.CertificadoFactory;
import br.insper.estudoPI.factory.TipoCertificado;
```

No `TODO [PADRAO] @Mock`:

```java
    @Mock
    private CertificadoFactory certificadoFactory;

    @Mock
    private Certificado certificado;
```

No `TODO [PADRAO] Testes dos padrões`:

```java
    @Test
    public void test_shouldGenerateCertificadoUsingFactory() {
        Curso curso = new Curso();
        curso.setId(1L);
        curso.setNome("Java Basico");

        Mockito.when(cursoRepository.findById(1L)).thenReturn(Optional.of(curso));
        Mockito.when(certificadoFactory.criar(TipoCertificado.CONCLUSAO, "Maria", curso)).thenReturn(certificado);
        Mockito.when(certificado.gerarTexto()).thenReturn("texto do certificado");

        String resultado = cursoService.gerarCertificado(1L, TipoCertificado.CONCLUSAO, "Maria");

        Assertions.assertEquals("texto do certificado", resultado);
        Mockito.verify(certificadoFactory).criar(TipoCertificado.CONCLUSAO, "Maria", curso);
    }
```

**Por que a factory é `@Component` e não um método `static`?** Para poder ser **mockada** aqui. Com `static`, o teste do service executaria a factory de verdade e você não conseguiria isolar o service.

## Passo 5 — Teste de integração (`CursoControllerTests`)

Import novo (junto dos outros `import static`):

```java
import static org.hamcrest.Matchers.containsString;
```

No `TODO [PADRAO] Testes de integração`:

```java
    @Test
    public void test_shouldGenerateCertificadoConclusao() throws Exception {
        Curso curso = cursoRepository.save(Curso.fromDto(criarDto("Java Basico")));

        mockMvc.perform(get("/cursos/" + curso.getId() + "/certificado")
                        .param("tipo", "CONCLUSAO")
                        .param("aluno", "Maria"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Maria concluiu o curso Java Basico")));
    }
```

## Testando na mão

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/cursos/1/certificado?tipo=CONCLUSAO&aluno=Maria" -Method Get
Invoke-RestMethod -Uri "http://localhost:8080/cursos/1/certificado?tipo=EXCELENCIA&aluno=Joao" -Method Get
```

## Pegadinhas

- **O `switch` com `->` sobre um enum não precisa de `default`** — o Java obriga você a cobrir todos os valores, e reclama na compilação se faltar um. Se você adicionar um tipo no enum, o compilador aponta a factory.
- **O enum na URL é case-sensitive:** `?tipo=conclusao` dá 400. Tem que ser `CONCLUSAO`.

---

# 3. Observer

## A ideia

Pense num canal do YouTube. Quando sai vídeo novo, **todos os inscritos são avisados**. O canal não sabe quem são os inscritos nem o que cada um faz com o aviso — só avisa. Inscrever alguém novo não muda nada no canal.

- **Observable** (o canal) = `CursoService`
- **Observers** (os inscritos) = `AuditoriaObserver`, `EmailObserver`
- **O aviso** = `atualizar(curso, evento)`

```
   CursoService (Observable)
     criar() / deletar()
            │
            └─ notificarObservadores(curso, evento)
                        │  for (observer : observers)
            ┌───────────┴───────────┐
            ▼                       ▼
   AuditoriaObserver          EmailObserver
    imprime log               "envia" e-mail
```

**Frase para explicar na prova:** *"O service não conhece quem é notificado, só a interface CursoObserver. Para adicionar um novo tipo de notificação, crio uma classe nova com @Component e ela passa a ser avisada automaticamente, sem mudar o service. Isso desacopla o service dos efeitos colaterais."*

É o exercício que o professor deixou no `PagamentoApplication.java` da API de pagamento: `PagamentoObserver`, `PagamentoObservable`, `AuditLoggerObserver`, `EmailNotifierObserver`.

## O truque do Spring: a `List`

```java
@Autowired(required = false)
private List<CursoObserver> observers = new ArrayList<>();
```

O Spring procura **todas** as classes `@Component` que implementam `CursoObserver` e coloca na lista. Nova classe observer → entra na lista sozinha.

- `required = false` → se não existir nenhum observer, o Spring não reclama.
- `= new ArrayList<>()` → nesse caso a lista fica vazia em vez de `null`. **Isso é de propósito**: o professor usou `if (observers != null)`, mas esse `if` cria dois branches que você teria que cobrir para chegar a 100%. Começando com lista vazia, o `if` não é necessário.

## Arquivos (já prontos na pasta)

| Arquivo | Papel |
|---|---|
| `CursoObserver.java` | interface de quem é avisado |
| `CursoObservable.java` | interface de quem avisa |
| `AuditoriaObserver.java` | imprime `AUDITORIA - Curso ID: ...` |
| `EmailObserver.java` | imprime `EMAIL ENVIADO - ...` |
| `ObserversTests.java` | captura o `System.out` e confere o texto |

## Passo 1 — Copiar

```powershell
robocopy padroes\observer\src src /E
```

## Passo 2 — `CursoService`

Imports novos:

```java
import br.insper.estudoPI.observer.CursoObservable;
import br.insper.estudoPI.observer.CursoObserver;

import java.util.ArrayList;
```

Na declaração da classe:

```java
public class CursoService implements CursoObservable {
```

No `TODO [PADRAO] Campos @Autowired`:

```java
    @Autowired(required = false)
    private List<CursoObserver> observers = new ArrayList<>();
```

No `TODO [PADRAO] Métodos dos padrões`:

```java
    @Override
    public void notificarObservadores(Curso curso, String evento) {
        for (CursoObserver observer : observers) {
            observer.atualizar(curso, evento);
        }
    }
```

**Substitua** os métodos `criar` e `deletar` por estes (a mudança é só a linha do `notificarObservadores`):

```java
    public Curso criar(CursoDto dto) {
        Curso curso = Curso.fromDto(dto);
        Curso salvo = cursoRepository.save(curso);
        notificarObservadores(salvo, "CRIADO");
        return salvo;
    }

    public void deletar(Long id) {
        Curso curso = buscarCurso(id);
        curso.setDeletado(true);
        cursoRepository.save(curso);
        notificarObservadores(curso, "DELETADO");
    }
```

Controller: **não muda nada**. O Observer não cria rota nova.

## Passo 3 — Testes unitários (`CursoServiceTests`)

Imports novos:

```java
import br.insper.estudoPI.observer.CursoObserver;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.util.ReflectionTestUtils;
```

No `TODO [PADRAO] @Mock`:

```java
    @Mock
    private CursoObserver observer;

    // O @InjectMocks não sabe preencher uma List. Então colocamos na mão
    // uma lista com o observer falso dentro do campo "observers" do service.
    @BeforeEach
    void configurarObservers() {
        ReflectionTestUtils.setField(cursoService, "observers", List.of(observer));
    }
```

Nos testes que já existem, descomente/adicione as linhas marcadas com `TODO [PADRAO] Observer`:

```java
    // no final de test_shouldCreateCursoWhenDtoIsValid
    Mockito.verify(observer).atualizar(curso, "CRIADO");

    // no final de test_shouldSetDeletadoTrueWhenCursoExists
    Mockito.verify(observer).atualizar(curso, "DELETADO");

    // no final de test_shouldThrowExceptionWhenCursoDoesNotExist
    Mockito.verify(observer, Mockito.never()).atualizar(Mockito.any(), Mockito.any());
```

Não precisa de teste novo no service: os testes de `criar` e `deletar` já passam pelo `for` com um observer na lista, o que cobre os dois branches do laço.

## Passo 4 — Teste de integração

**Nada novo.** Os observers rodam de verdade quando o teste chama `POST` e `DELETE` — você vai ver as linhas `AUDITORIA -` e `EMAIL ENVIADO -` no console do teste.

## Testando na mão

Faça um `POST` ou `DELETE` e olhe o log:

```powershell
docker logs estudopi
```

## Pegadinhas

- **O `@Component` nos observers é obrigatório.** Sem ele o Spring não acha a classe e ela nunca entra na lista — e nenhum erro aparece, simplesmente não imprime.
- **Chame `notificarObservadores` DEPOIS do `save`**, com o objeto salvo. Antes do `save` o curso ainda não tem `id`.
- **O nome do campo no `ReflectionTestUtils.setField`** tem que ser exatamente `"observers"`, igual ao do service.

---

# CursoService com os três padrões juntos (referência)

Para ver onde cada peça mora quando tudo está aplicado:

```java
package br.insper.estudoPI.service;

import br.insper.estudoPI.dto.CursoDto;
import br.insper.estudoPI.entity.Curso;
import br.insper.estudoPI.exception.CursoNaoEncontradoException;
import br.insper.estudoPI.factory.Certificado;
import br.insper.estudoPI.factory.CertificadoFactory;
import br.insper.estudoPI.factory.TipoCertificado;
import br.insper.estudoPI.observer.CursoObservable;
import br.insper.estudoPI.observer.CursoObserver;
import br.insper.estudoPI.repository.CursoRepository;
import br.insper.estudoPI.strategy.CalculadoraDesconto;
import br.insper.estudoPI.strategy.DescontoInvalidoException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class CursoService implements CursoObservable {

    @Autowired
    private CursoRepository cursoRepository;

    @Autowired
    private Map<String, CalculadoraDesconto> calculadoras;          // Strategy

    @Autowired
    private CertificadoFactory certificadoFactory;                  // Factory

    @Autowired(required = false)
    private List<CursoObserver> observers = new ArrayList<>();      // Observer

    public List<Curso> listar(String nome) {
        if (nome == null || nome.isBlank()) {
            return cursoRepository.findByDeletadoFalse();
        }
        return cursoRepository.findByNomeStartingWithIgnoreCaseAndDeletadoFalse(nome);
    }

    public Curso criar(CursoDto dto) {
        Curso curso = Curso.fromDto(dto);
        Curso salvo = cursoRepository.save(curso);
        notificarObservadores(salvo, "CRIADO");
        return salvo;
    }

    public void deletar(Long id) {
        Curso curso = buscarCurso(id);
        curso.setDeletado(true);
        cursoRepository.save(curso);
        notificarObservadores(curso, "DELETADO");
    }

    public BigDecimal calcularPrecoFinal(Long id, String tipoDesconto) {
        Curso curso = buscarCurso(id);
        CalculadoraDesconto calculadora = calculadoras.get(tipoDesconto.toUpperCase());
        if (calculadora == null) {
            throw new DescontoInvalidoException("Tipo de desconto invalido: " + tipoDesconto);
        }
        return calculadora.aplicar(curso.getPreco());
    }

    public String gerarCertificado(Long id, TipoCertificado tipo, String aluno) {
        Curso curso = buscarCurso(id);
        Certificado certificado = certificadoFactory.criar(tipo, aluno, curso);
        return certificado.gerarTexto();
    }

    @Override
    public void notificarObservadores(Curso curso, String evento) {
        for (CursoObserver observer : observers) {
            observer.atualizar(curso, evento);
        }
    }

    private Curso buscarCurso(Long id) {
        return cursoRepository.findById(id)
                .orElseThrow(() -> new CursoNaoEncontradoException("Curso com ID " + id + " não encontrado"));
    }
}
```

Testes necessários para 100% nessa versão: os 6 da base + 2 do Strategy + 1 do Factory = **9**.
