# JourneyFlow

Dashboard para acompanhar a integração de equipes, visualizar alertas e apoiar a jornada de colaboradores. O projeto reúne uma interface web em JavaScript e um backend em Java com Spring Boot.

## Funcionalidades da interface

- Visão geral com indicadores, gráficos e alertas recentes.
- Consulta, busca, ordenação e filtragem de colaboradores e alertas.
- Detalhes e cadastro de colaboradores.
- Área do colaborador com conversa, pontos, recompensas e histórico.
- Preferências de perfil e tema salvas no navegador.

## Tecnologias

- **Frontend:** HTML, CSS e JavaScript com módulos ES; Chart.js para gráficos.
- **Backend:** Java 21, Spring Boot, Spring Web MVC, Spring Data JPA e Spring Security.
- **Banco de dados:** PostgreSQL.
- **Build do backend:** Maven Wrapper.

## Estrutura

```text
frontend/                  Interface web e dados demonstrativos
  assets/                  Imagens e ícones
  css/                     Estilos da aplicação
  data/mock-data.js        Dados usados atualmente pela interface
  js/                      Componentes, páginas e serviços do frontend
backend/journeyflow/       Aplicação Spring Boot
  src/main/java/           Controllers, serviços, repositórios e entidades
  src/main/resources/      Configuração da aplicação
  pom.xml                  Dependências e configuração Maven
docs/modelagem.md          Rascunho da modelagem do domínio
```

## Executar o frontend

É necessário ter Node.js 18 ou superior. O servidor de desenvolvimento serve os arquivos estáticos e encaminha `/__api/*` ao backend para evitar CORS sem alterar a configuração do Spring Boot.

No Windows PowerShell:

```powershell
cd frontend
npm start
```

No macOS ou Linux:

```bash
cd frontend
npm start
```

Abra <http://localhost:8000>. Se o backend estiver em outro endereço, defina `JOURNEYFLOW_API_URL` antes de iniciar o servidor (por padrão, `http://localhost:8080`).

O login aceita contas de gestor e colaborador, autenticadas em `/auth/login`. O gestor cria a conta do colaborador em **Funcionários > Funcionário**, definindo e compartilhando com ele o e-mail e a senha inicial. O colaborador entra com essas credenciais; seu perfil, saldo/histórico de pontos, mensagens e progresso das pílulas de conhecimento são carregados pelos endpoints existentes. As pílulas são liberadas conforme o dia da jornada, e a conclusão é registrada ao acertar o questionário. O gestor também consome os endpoints de alertas, relatos, gerente e loja, e pode confirmar alertas. O token fica em `sessionStorage` e é encaminhado pelo proxy; não use este servidor de desenvolvimento como proxy público de produção. As recompensas ainda usam os dados demonstrativos de `frontend/data/mock-data.js`.

## Executar o backend

Requisitos: JDK 21 e acesso a uma instância PostgreSQL compatível com a configuração da aplicação.

Configure as propriedades de conexão do datasource para o seu ambiente em `backend/journeyflow/src/main/resources/application.properties` antes de iniciar. Não compartilhe credenciais reais no repositório.

No Windows PowerShell:

```powershell
cd backend/journeyflow
.\mvnw.cmd spring-boot:run
```

No macOS ou Linux:

```bash
cd backend/journeyflow
./mvnw spring-boot:run
```

A aplicação Spring Boot usa a porta padrão `8080`. As rotas do backend exigem autenticação JWT, exceto `/auth/**`. Faça login com uma conta de gerente cadastrada para abrir o painel integrado.

## Testes

Para executar os testes do backend com o Maven Wrapper:

```bash
cd backend/journeyflow
./mvnw test
```

No Windows PowerShell, use `.\mvnw.cmd test`.
