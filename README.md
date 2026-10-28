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

É necessário ter Python 3 instalado. Na raiz do repositório, inicie um servidor estático:

```bash
python -m http.server 8000 --directory frontend
```

Abra <http://localhost:8000> no navegador. Também é possível usar outra ferramenta de servidor estático, desde que a raiz servida seja a pasta `frontend`; abrir `index.html` diretamente pode impedir o carregamento dos módulos JavaScript.

A interface usa dados demonstrativos definidos em `frontend/data/mock-data.js`. As interações e preferências são mantidas em memória ou no `localStorage` do navegador. Atualmente, o frontend não está integrado aos endpoints do backend.

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

A aplicação Spring Boot usa a porta padrão `8080`. A configuração atual de segurança exige autenticação nas rotas, exceto em `/auth/**`; os controllers disponíveis ainda não declaram operações HTTP. Portanto, o backend é uma base em desenvolvimento, e a interface demonstrativa não depende dele.

## Testes

Para executar os testes do backend com o Maven Wrapper:

```bash
cd backend/journeyflow
./mvnw test
```

No Windows PowerShell, use `.\mvnw.cmd test`.
