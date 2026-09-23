# estudoPI — Repositório base + cola da prova

API Spring Boot com Postgres, testes com 100% no service, Docker, docker compose e deploy automático na VM com GitHub Actions.

Tudo que é comando está pronto pra copiar e colar no **PowerShell**, na pasta que tem o `pom.xml`.

---

## Mapa do repositório

```
estudoPI/
├── .github/workflows/
│   ├── tests.yml            ← roda no Pull Request: testes + cobertura comentada no PR
│   └── deploy.yml           ← roda no merge: testa, monta imagem, sobe no DockerHub, roda na VM
├── src/main/java/br/insper/estudoPI/
│   ├── EstudoPiApplication.java
│   ├── controller/CursoController.java
│   ├── dto/CursoDto.java
│   ├── entity/Curso.java
│   ├── exception/CursoNaoEncontradoException.java
│   ├── repository/CursoRepository.java
│   └── service/CursoService.java
├── src/main/resources/application.properties   ← tudo sensível vem de variável de ambiente
├── src/test/java/br/insper/estudoPI/
│   ├── controller/CursoControllerTests.java    ← integração (Testcontainers)
│   └── service/CursoServiceTests.java          ← unitário (Mockito), 100%
├── padroes/                 ← Strategy, Factory, Observer. NÃO compila. Apague se não cair.
│   └── PADROES.md
├── .env.example             ← modelo do .env (senha do banco pro docker compose)
├── docker-compose.yml       ← app + banco com um comando
├── Dockerfile
├── lombok.config            ← faz o JaCoCo ignorar getters/setters do Lombok
└── pom.xml                  ← JaCoCo reprova o build se o service tiver < 100%
```

### Os marcadores no código

Todo lugar onde você vai mexer na prova tem um comentário:

- **`TODO [PROVA]`** → adaptar ao enunciado (campos, rotas, consultas, testes novos)
- **`TODO [PADRAO]`** → onde encaixar Strategy / Factory / Observer

No IntelliJ, **Alt+6** abre a janela **TODO** com todos eles listados. Clique e vai direto pra linha.

---

# PARTE 1 — HOJE À NOITE (fazer uma vez só)

O objetivo de hoje é **ver o pipeline inteiro ficar verde e a API responder na VM**. Se isso funcionar hoje, amanhã você só escreve código.

## 1.1 Colocar os arquivos no seu projeto

Extraia o zip **por cima** da pasta do seu projeto (a que tem o `pom.xml`), aceitando substituir. Seu `mvnw`, `mvnw.cmd` e `.mvn/` continuam lá.

Depois **apague** o teste que o Initializr gerou (ele sobe o Spring sem banco e quebra o build):

```powershell
Remove-Item src\test\java\br\insper\estudoPI\EstudoPiApplicationTests.java
```

No IntelliJ: clique no ícone do Maven (o **m** na barra da direita) → botão de **recarregar**.

## 1.2 Conferir que compila e os testes passam

Abra o **Docker Desktop** antes (o teste de integração precisa dele).

```powershell
.\mvnw.cmd clean install
```

Tem que terminar com `BUILD SUCCESS`. Abra o relatório de cobertura:

```powershell
start tests\index.html
```

> Se o `mvnw.cmd` reclamar de `JAVA_HOME`: use o painel do Maven no IntelliJ (ícone **m** → estudoPI → Lifecycle → duplo clique em `install`). Faz a mesma coisa.

## 1.3 Criar o repositório no GitHub e subir

Crie um repositório **vazio** no GitHub (sem README, sem .gitignore). Depois:

```powershell
# conferir onde está a raiz do git -> TEM que ser a pasta que tem o pom.xml
git rev-parse --show-toplevel
```

> ⚠️ **Se a raiz do git for uma pasta ACIMA da que tem o `pom.xml`**, os workflows não vão achar o projeto. No seu IntelliJ a estrutura aparece como `estudoPI\estudoPI\pom.xml` — confira. Se for o caso, rode os comandos abaixo **dentro** da pasta do `pom.xml` e apague a pasta `.git` da pasta de cima.

```powershell
git init
git add .
git commit -m "estrutura base"
git branch -M main
git remote add origin https://github.com/SEU-USUARIO/estudoPI.git
git push -u origin main
```

No GitHub, na página do repositório, o `pom.xml` e a pasta `.github` têm que aparecer **na primeira tela**, não dentro de outra pasta.

## 1.4 Criar os 5 secrets

GitHub → repositório → **Settings → Secrets and variables → Actions → New repository secret**

| Nome | Valor | Onde pegar |
|---|---|---|
| `DOCKERHUB_USERNAME` | seu usuário do DockerHub | — |
| `DOCKERHUB_TOKEN` | um token | hub.docker.com → foto → **Account settings → Personal access tokens → Generate** (permissão Read & Write) |
| `HOST_TEST` | IP público da VM | console da AWS → EC2 → sua instância → *Public IPv4 address* |
| `KEY_TEST` | conteúdo **inteiro** do `.pem` | comando abaixo |
| `DB_PASSWORD` | `senha` | a senha que o Postgres da VM vai usar |

Copiar o conteúdo do `.pem` para a área de transferência:

```powershell
Get-Content C:\caminho\da\chave.pem -Raw | Set-Clipboard
```

Cole no secret. Tem que incluir as linhas `-----BEGIN ...` e `-----END ...`.

O `GITHUB_TOKEN` que aparece no `tests.yml` é automático — **não crie**.

## 1.5 Preparar a VM (só instalar o Docker)

Liberar a porta **8080** na AWS: EC2 → sua instância → aba **Security** → clique no Security Group → **Edit inbound rules → Add rule** → *Custom TCP*, porta `8080`, origem `0.0.0.0/0` → Save.

Entrar na VM:

```powershell
# a chave precisa de permissão restrita, senão o ssh recusa (só na primeira vez)
icacls "C:\caminho\da\chave.pem" /inheritance:r
icacls "C:\caminho\da\chave.pem" /grant:r "$($env:USERNAME):(R)"

ssh -i "C:\caminho\da\chave.pem" ubuntu@IP-DA-VM
```

Já dentro da VM:

```bash
# instala o Docker (script oficial)
curl -fsSL https://get.docker.com | sudo sh

# deixa o usuário ubuntu usar docker sem sudo
sudo usermod -a -G docker ubuntu
newgrp docker

# conferir
docker ps
```

**Só isso.** A rede `rede` e o container `postgres-aula` o próprio `deploy.yml` cria na primeira execução.

> Se a VM já tem um `postgres-aula` da aula com **outra senha**, a aplicação não vai conseguir conectar. Nesse caso, na VM: `docker rm -f postgres-aula` e deixe o pipeline recriar.

## 1.6 Disparar o deploy e testar o PR

Como o `deploy.yml` roda em todo push na `main`, o push do passo 1.3 **já disparou** o deploy. Vá em **Actions** no GitHub e acompanhe. Se ele rodou antes de você criar os secrets, clique no workflow → **Re-run all jobs**.

Quando ficar verde:

```powershell
Invoke-RestMethod -Uri "http://IP-DA-VM:8080/cursos" -Method Get
```

Resposta vazia (sem erro) = **deu tudo certo**.

Agora teste o pipeline de PR:

```powershell
git checkout -b teste-pr
# faça qualquer mudança pequena, ex: um comentário no CursoController
git add .
git commit -m "teste do pipeline de PR"
git push -u origin teste-pr
```

No GitHub → **Compare & pull request** → **Create pull request**. O workflow `Testes` roda e o bot comenta a cobertura. Depois dê **Merge** e veja o `Deploy` rodar de novo.

```powershell
git checkout main
git pull
```

**Se chegou aqui com tudo verde, a parte de infraestrutura da prova está pronta.**

---

# PARTE 2 — NA PROVA

## 2.1 Ler o enunciado e responder

- Qual é a **entidade**? (Curso? Livro? Produto?) Quais **campos**?
- Quais **rotas**? Algum **filtro** (startWith, containing, por categoria)?
- **Deleção lógica** ou física?
- Pede **status code** específico (201, 204)?
- Cai algum **padrão**? → ver `padroes/PADROES.md`

## 2.2 Criar a branch

```powershell
git checkout main
git pull
git checkout -b prova
```

Fazendo tudo numa branch e abrindo **um** PR no final, você cumpre "rota criada via Pull Request" e o pipeline de testes roda sobre tudo.

## 2.3 Se o domínio for outro (ex: Livro em vez de Curso)

Nessa ordem:

1. **Renomear as classes** — clique no nome da classe → **Shift+F6** → digite o novo nome. Faça em: `Curso`, `CursoDto`, `CursoRepository`, `CursoService`, `CursoController`, `CursoNaoEncontradoException`, `CursoServiceTests`, `CursoControllerTests`. O IntelliJ renomeia o arquivo e todos os usos.
2. **Trocar os textos** — **Ctrl+Shift+R** (Replace in Path), marque **Match case**:
   - `"/cursos"` → `"/livros"`
   - `name = "cursos"` → `name = "livros"`
   - `Curso com ID` → `Livro com ID`
3. **Ajustar os campos** na entidade, no DTO e no `fromDto()`.
4. **Ajustar os métodos do repository**: o nome depende do nome do campo. Se trocou `nome` por `titulo`, o método vira `findByTituloStartingWithIgnoreCaseAndDeletadoFalse`. **Esse é o erro mais comum**: ele só aparece quando a aplicação sobe.
5. Ajustar o `criarDto()` do teste de integração.

## 2.4 Ciclo de trabalho (repita para cada rota)

1. Método no **repository** (se precisar de consulta nova)
2. Método no **service**
3. **Teste unitário** — um por caminho de cada `if`
4. Rota no **controller**
5. **Teste de integração**
6. Rodar:

```powershell
.\mvnw.cmd clean install
```

Rodar só os testes unitários (rápido, sem Docker):

```powershell
.\mvnw.cmd test "-Dtest=CursoServiceTests"
```

> No PowerShell, argumentos `-D...` devem ir **entre aspas**, senão ele pode quebrar o argumento no meio.

No IntelliJ também dá: botão direito na classe de teste → **More Run/Debug → Run with Coverage**. Mostra as linhas cobertas em verde e as não cobertas em vermelho direto no código.

## 2.5 Subir tudo

```powershell
git add .
git commit -m "feat: implementa rotas da prova"
git push -u origin prova
```

GitHub → **Compare & pull request** → **Create pull request** → espere o check **verde** → **tire um print** → **Merge pull request** → aba **Actions** → espere o **Deploy** ficar verde → teste na VM:

```powershell
Invoke-RestMethod -Uri "http://IP-DA-VM:8080/cursos" -Method Get
```

---

# PARTE 3 — COMANDOS

## Maven

```powershell
.\mvnw.cmd clean test                      # compila + roda testes + gera relatório
.\mvnw.cmd clean install                   # tudo acima + exige 100% no service
.\mvnw.cmd test "-Dtest=CursoServiceTests" # só uma classe de teste
.\mvnw.cmd clean package "-DskipTests"     # só gera o .jar
start tests\index.html                     # abre o relatório de cobertura
```

## Docker local — jeito 1: docker compose (app + banco num comando)

```powershell
Copy-Item .env.example .env            # só na primeira vez
docker compose up -d --build           # constrói a imagem e sobe os dois
docker compose ps                      # status
docker compose logs -f app             # log da aplicação (Ctrl+C sai)
docker compose down                    # derruba (mantém os dados)
docker compose down -v                 # derruba e APAGA os dados do banco
```

Mudou o código? Rode `docker compose up -d --build` de novo.

## Docker local — jeito 2: comando por comando (o jeito da aula)

É o que o professor mostrou e o que o `deploy.yml` faz na VM. Bom saber explicar.

```powershell
# 1. rede: dentro dela, os containers se acham pelo NOME
docker network create -d bridge rede

# 2. banco
docker run -d --name postgres-aula -e POSTGRES_DB=auladb -e POSTGRES_USER=usuario -e POSTGRES_PASSWORD=senha --network rede -p 5432:5432 postgres

# 3. imagem da aplicação (nome de imagem SEMPRE minúsculo)
docker build -t estudopi .

# 4. aplicação na mesma rede, apontando pro banco pelo nome
docker run -d -p 8080:8080 --network rede -e DB_HOST=postgres-aula -e DB_PASSWORD=senha --name estudopi estudopi

# 5. acompanhar
docker logs -f estudopi
```

Refazer depois de mudar o código:

```powershell
docker rm -f estudopi
docker build -t estudopi .
docker run -d -p 8080:8080 --network rede -e DB_HOST=postgres-aula -e DB_PASSWORD=senha --name estudopi estudopi
```

**Não misture os dois jeitos ao mesmo tempo** (os nomes e as portas conflitam). Para trocar:

```powershell
docker compose down
docker rm -f estudopi postgres-aula
```

## Rodar pela IDE (sem Docker na aplicação)

Suba só o banco (jeito 1: `docker compose up -d postgres-aula`, ou jeito 2: o comando do passo 2). Depois:

```powershell
$env:DB_PASSWORD="senha"
.\mvnw.cmd spring-boot:run
```

Ou no IntelliJ: **Run → Edit Configurations → EstudoPiApplication → Environment variables** → `DB_PASSWORD=senha`.

## Testar as rotas (PowerShell)

`curl` no PowerShell **não é** o curl do Linux. Use `Invoke-RestMethod`:

```powershell
# POST
Invoke-RestMethod -Uri "http://localhost:8080/cursos" -Method Post -ContentType "application/json; charset=utf-8" -Body '{"nome":"Java Basico","descricao":"Introducao","categoria":"Programacao","cargaHoraria":40,"preco":200.00,"instrutor":"Eduardo"}'
Invoke-RestMethod -Uri "http://localhost:8080/cursos" -Method Post -ContentType "application/json; charset=utf-8" -Body '{"nome":"Java Avancado","cargaHoraria":60,"preco":400.00}'
Invoke-RestMethod -Uri "http://localhost:8080/cursos" -Method Post -ContentType "application/json; charset=utf-8" -Body '{"nome":"Python","cargaHoraria":30,"preco":150.00}'

# GET todos
Invoke-RestMethod -Uri "http://localhost:8080/cursos" -Method Get

# GET com filtro startWith -> só os 2 de Java
Invoke-RestMethod -Uri "http://localhost:8080/cursos?nome=Java" -Method Get

# DELETE lógico
Invoke-RestMethod -Uri "http://localhost:8080/cursos/1" -Method Delete

# GET de novo -> o 1 sumiu
Invoke-RestMethod -Uri "http://localhost:8080/cursos" -Method Get
```

Na VM, troque `localhost` pelo IP da VM.

**Acentos:** se chegarem estragados, mande como bytes UTF-8:

```powershell
$body = [System.Text.Encoding]::UTF8.GetBytes('{"nome":"Introdução","cargaHoraria":10}')
Invoke-RestMethod -Uri "http://localhost:8080/cursos" -Method Post -ContentType "application/json; charset=utf-8" -Body $body
```

## Olhar dentro do banco

```powershell
docker exec -it postgres-aula psql -U usuario -d auladb
```

```sql
\dt
SELECT id, nome, deletado FROM cursos;
\q
```

O curso deletado aparece com `deletado = t`. **Isso prova que a deleção é lógica.**

## Git

```powershell
git status                                  # o que mudou
git checkout -b nome-da-branch              # nova branch
git add .
git commit -m "mensagem"
git push -u origin nome-da-branch           # primeira vez da branch
git push                                    # próximas vezes
git checkout main; git pull                 # voltar e atualizar a main
```

## VM

```powershell
ssh -i "C:\caminho\da\chave.pem" ubuntu@IP-DA-VM
```

```bash
docker ps                              # tem que ter estudopi e postgres-aula rodando
docker logs estudopi                   # log da aplicação
docker logs estudopi | tail -50        # só o final
curl http://localhost:8080/cursos      # testar de dentro da VM
docker network inspect rede            # os dois containers têm que estar aqui
```

Deploy manual na VM (se o pipeline falhar e você precisar mostrar funcionando):

```bash
docker network create -d bridge rede
docker run -d --name postgres-aula --restart unless-stopped -e POSTGRES_DB=auladb -e POSTGRES_USER=usuario -e POSTGRES_PASSWORD=senha --network rede postgres
docker rm -f estudopi
docker run -d -p 8080:8080 --restart unless-stopped --network rede -e DB_HOST=postgres-aula -e DB_PASSWORD=senha --name estudopi SEU-USUARIO/estudopi-ci:TAG
```

A `TAG` é o hash do commit — aparece no log do step **Build and push** no Actions, ou em hub.docker.com.

## Limpeza

```powershell
docker ps -a                  # todos os containers, inclusive parados
docker rm -f NOME             # remove um container
docker images                 # imagens
docker system prune -a        # apaga TUDO que não está rodando (cuidado)
```

---

# PARTE 4 — VARIAÇÕES QUE PODEM CAIR

Cada uma com: repository → service → testes unitários → controller → teste de integração.

## Buscar por id (sem mostrar deletado)

**Dica:** colocar a condição **no repository** em vez de um `if` no service → menos branches → menos testes.

```java
// CursoRepository
Optional<Curso> findByIdAndDeletadoFalse(Long id);      // import java.util.Optional;

// CursoService
public Curso buscarPorId(Long id) {
    return cursoRepository.findByIdAndDeletadoFalse(id)
            .orElseThrow(() -> new CursoNaoEncontradoException("Curso com ID " + id + " não encontrado"));
}

// CursoController
@GetMapping("/{id}")
public Curso buscarPorId(@PathVariable Long id) {
    return cursoService.buscarPorId(id);
}
```

```java
// CursoServiceTests
@Test
public void test_shouldReturnCursoWhenBuscarPorIdExists() {
    Curso curso = new Curso();
    curso.setId(1L);
    Mockito.when(cursoRepository.findByIdAndDeletadoFalse(1L)).thenReturn(Optional.of(curso));

    Assertions.assertEquals(1L, cursoService.buscarPorId(1L).getId());
}

@Test
public void test_shouldThrowWhenBuscarPorIdDoesNotExist() {
    Mockito.when(cursoRepository.findByIdAndDeletadoFalse(99L)).thenReturn(Optional.empty());

    Assertions.assertThrows(CursoNaoEncontradoException.class, () -> cursoService.buscarPorId(99L));
}

// CursoControllerTests
@Test
public void test_shouldReturnCursoById() throws Exception {
    Curso curso = cursoRepository.save(Curso.fromDto(criarDto("Java Basico")));

    mockMvc.perform(get("/cursos/" + curso.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nome").value("Java Basico"));
}
```

## Atualizar (PUT)

```java
// CursoService (usa o buscarPorId acima)
public Curso atualizar(Long id, CursoDto dto) {
    Curso curso = buscarPorId(id);
    curso.setNome(dto.getNome());
    curso.setDescricao(dto.getDescricao());
    curso.setCategoria(dto.getCategoria());
    curso.setCargaHoraria(dto.getCargaHoraria());
    curso.setPreco(dto.getPreco());
    curso.setInstrutor(dto.getInstrutor());
    return cursoRepository.save(curso);
}

// CursoController
@PutMapping("/{id}")
public Curso atualizar(@PathVariable Long id, @RequestBody CursoDto dto) {
    return cursoService.atualizar(id, dto);
}
```

```java
// CursoServiceTests
@Test
public void test_shouldUpdateCursoWhenExists() {
    Curso curso = new Curso();
    curso.setId(1L);
    curso.setNome("Antigo");

    CursoDto dto = new CursoDto();
    dto.setNome("Novo");

    Mockito.when(cursoRepository.findByIdAndDeletadoFalse(1L)).thenReturn(Optional.of(curso));
    Mockito.when(cursoRepository.save(curso)).thenReturn(curso);

    Curso resultado = cursoService.atualizar(1L, dto);

    Assertions.assertEquals("Novo", resultado.getNome());
}

// CursoControllerTests
@Test
public void test_shouldUpdateCurso() throws Exception {
    Curso curso = cursoRepository.save(Curso.fromDto(criarDto("Antigo")));

    mockMvc.perform(put("/cursos/" + curso.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(criarDto("Novo"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nome").value("Novo"));
}
```

O caso "não encontrado" do `atualizar` já é coberto pelo teste do `buscarPorId`, porque não há `if` novo.

## Validação que devolve 400

Ex: "não permitir preço negativo".

```java
// exception/ValidacaoException.java
package br.insper.estudoPI.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ValidacaoException extends RuntimeException {
    public ValidacaoException(String mensagem) {
        super(mensagem);
    }
}
```

Imports no `CursoService` e no `CursoServiceTests`: `import br.insper.estudoPI.exception.ValidacaoException;` e, no service, `import java.math.BigDecimal;`

```java
// CursoService.criar - primeira linha do método
if (dto.getPreco() != null && dto.getPreco().compareTo(BigDecimal.ZERO) < 0) {
    throw new ValidacaoException("Preco nao pode ser negativo");
}
```

Esse `if` tem **dois** pontos de decisão (`!= null` e `< 0`) → 4 saídas → precisa de testes para: preço nulo, preço negativo, preço positivo. O teste de criar que já existe cobre o positivo. Adicione:

```java
@Test
public void test_shouldThrowWhenPrecoIsNegative() {
    CursoDto dto = new CursoDto();
    dto.setNome("Java");
    dto.setPreco(new BigDecimal("-10"));

    Assertions.assertThrows(ValidacaoException.class, () -> cursoService.criar(dto));
    Mockito.verify(cursoRepository, Mockito.never()).save(Mockito.any());
}

@Test
public void test_shouldCreateWhenPrecoIsNull() {
    CursoDto dto = new CursoDto();
    dto.setNome("Gratuito");

    Mockito.when(cursoRepository.save(Mockito.any(Curso.class))).thenAnswer(inv -> inv.getArgument(0));

    Assertions.assertEquals("Gratuito", cursoService.criar(dto).getNome());
}
```

`thenAnswer(inv -> inv.getArgument(0))` = "devolva o mesmo objeto que recebeu". Útil quando você não quer montar o retorno na mão.

## Outros filtros no repository

| Enunciado pede | Método no repository |
|---|---|
| nome **começa** com | `findByNomeStartingWithIgnoreCaseAndDeletadoFalse(String nome)` |
| nome **contém** | `findByNomeContainingIgnoreCaseAndDeletadoFalse(String nome)` |
| por categoria exata | `findByCategoriaAndDeletadoFalse(String categoria)` |
| preço até X | `findByPrecoLessThanEqualAndDeletadoFalse(BigDecimal preco)` |
| preço entre X e Y | `findByPrecoBetweenAndDeletadoFalse(BigDecimal min, BigDecimal max)` |
| ordenado por nome | `findByDeletadoFalseOrderByNomeAsc()` |

---

# PARTE 5 — QUANDO DÁ ERRO

| Erro | Causa → solução |
|---|---|
| `Could not find a valid Docker environment` | Docker Desktop fechado. Abra e espere ficar verde |
| `Connection refused` / `Connection to localhost:5432 refused` | Banco não está rodando, ou faltou `--network rede` + `-e DB_HOST=postgres-aula` |
| `password authentication failed` | Faltou `-e DB_PASSWORD=...`, ou o Postgres foi criado com outra senha → `docker rm -f postgres-aula` e suba de novo |
| `required variable DB_PASSWORD is missing` (compose) | Faltou o `.env` → `Copy-Item .env.example .env` |
| `port is already allocated` | Já tem algo na porta. `docker ps -a` e `docker rm -f NOME` |
| `Conflict. The container name ... is already in use` | `docker rm -f NOME` antes |
| `invalid reference format` | Nome de imagem com maiúscula. Use `estudopi` |
| `No property 'X' found for type 'Curso'` | Nome de método do repository não bate com o nome do campo |
| `Rule violated for class ...Service: lines/branches covered ratio is 0.xx` | Falta teste. Abra `tests\index.html`, clique no service: linha **amarela** = falta um lado do `if`, **vermelha** = nunca executou |
| `UnnecessaryStubbingException` | Você fez `Mockito.when(...)` de algo que o teste nunca chama. Apague essa linha |
| `NullPointerException` num teste unitário | Faltou um `@Mock` para algum `@Autowired` do service |
| Teste de integração falha com contagem errada | Faltou o `@BeforeEach` com `deleteAll()` |
| `NoClassDefFoundError` / `NoSuchMethodError` no Testcontainers | Versões desencontradas. Coloque as três dependências do Testcontainers na mesma versão |
| `jacoco.xml` não encontrado no Actions | `outputDirectory` do pom tem que ser `${project.basedir}/tests` |
| Workflow não aparece no Actions | `.github` não está na raiz do repositório (ver 1.3) |
| Deploy falha no SSH (`handshake failed`) | `KEY_TEST` incompleto (faltou BEGIN/END) ou `HOST_TEST` errado |
| Deploy verde mas navegador não abre | Porta 8080 fechada no Security Group |
| Deploy verde mas `docker ps` na VM não mostra `estudopi` | A app caiu ao subir → `docker logs estudopi` na VM |
| Getter `getDeletado()` não existe | Para `boolean` o Lombok gera `isDeletado()` |
| `JAVA_HOME not found` | Use o painel Maven do IntelliJ (ícone **m**) |
