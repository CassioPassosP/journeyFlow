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
frontend/                  Interface web
  assets/                  Imagens e ícones
  css/                     Estilos da aplicação
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

O login aceita contas de gestor e colaborador, autenticadas em `/auth/login`. O gestor cria a conta do colaborador em **Funcionários > Funcionário**, definindo e compartilhando com ele o e-mail e a senha inicial. O colaborador entra com essas credenciais; seu perfil, saldo/histórico de pontos, mensagens e progresso das pílulas de conhecimento são carregados pelos endpoints existentes. As pílulas são liberadas conforme o dia da jornada, e a conclusão é registrada ao acertar o questionário. O gestor também consome os endpoints de alertas, gerente e loja, e pode confirmar alertas. O token fica em `sessionStorage` e é encaminhado pelo proxy; não use este servidor de desenvolvimento como proxy público de produção. A interface exibe somente registros recebidos do backend; recursos sem suporte na API, como recompensas, não são simulados.

## Classificação privada de conversas

Ao concluir uma conversa em `/chat/finish/{employeeId}`, o Gemini classifica somente o tipo, o destino e a prioridade do acompanhamento. Conversas neutras ou sem informação suficiente são encerradas sem alerta. Assuntos de rotina são encaminhados ao papel `MANAGER`; relatos de abuso, assédio, discriminação, violência, retaliação ou sobre um gestor são destinados ao papel `RH` e continuam exigindo autorização do colaborador para encaminhar o pedido de conversa privada.

Os alertas guardam o identificador do colaborador, a unidade, a prioridade (`NORMAL`, `ATTENTION` ou `FOLLOW_UP`) e uma sugestão genérica; não guardam transcrição, resumo nem detalhes do relato. O histórico de conversa só pode ser consultado pelo próprio colaborador. A consulta e as alterações dos alertas ficam limitadas à unidade e ao destino compatível com o papel do gestor autenticado. Cada unidade precisa ter pelo menos um registro de gestor com `position` igual a `MANAGER` (ou `GERENTE`) e, para encaminhamentos de RH, um com `position` igual a `RH` (ou `HR`).

Antes de executar a versão com alertas sem relatório, aplique uma vez o script [V2026_11_05__alerts_without_reports.sql](backend/journeyflow/src/main/resources/db/manual/V2026_11_05__alerts_without_reports.sql) no PostgreSQL. O projeto usa `spring.jpa.hibernate.ddl-auto=none`, então a alteração do esquema não é automática.

## Executar o backend

Requisitos: JDK 21 e acesso a uma instância PostgreSQL. Na raiz do repositório, copie `.env.example` para `.env` e preencha as credenciais do banco, a chave JWT e a chave da API Gemini. O arquivo `.env` é local e não deve ser compartilhado nem enviado ao Git.

O backend importa esse arquivo ao iniciar; as propriedades correspondentes ficam em `backend/journeyflow/src/main/resources/application.properties`.

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
