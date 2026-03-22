# MeChamaAPP — Documento de Contexto do Projeto

## Visão Geral

O MeChamaAPP é um super-app mobile-first que conecta clientes a prestadores de serviços,
com um ecossistema financeiro próprio integrado (carteira digital, stablecoin, consórcio).

O app compete diretamente com GetNinjas, Uber (serviços), iFood (modelo de plataforma),
BTG Pactual, Nubank e XP Investimentos no futuro, mas começa pelo marketplace de serviços.

A ideia central é que todo o dinheiro que circula na plataforma (pagamentos de serviços,
consórcios, lances, aluguéis de crédito) fique retido no ecossistema da plataforma o
máximo de tempo possível, gerando rendimento para a tesouraria da empresa.

---

## Stack Tecnológica

| Camada            | Tecnologia                  |
|-------------------|-----------------------------|
| Backend principal | Java + Spring Boot          |
| Banco de dados    | PostgreSQL                  |
| Cache / Filas     | Redis + RabbitMQ (futuro)   |
| Mobile            | React Native (iOS + Android)|
| Web (painel)      | React.js / Next.js          |
| Cloud             | AWS ou GCP                  |
| Containers        | Docker (Kubernetes no futuro)|

---

## Princípios de Desenvolvimento

- Desenvolver **uma funcionalidade por vez**, completamente (model → service → controller → telas).
- Antes de iniciar cada funcionalidade, explicar como ela se integra com o que já foi feito.
- Arquitetura monolito modular no início, pronta para ser quebrada em microsserviços.
- Mobile-first: toda UI é pensada primeiro para o app em React Native.
- Nunca reescrever código já funcional; sempre evoluir a arquitetura existente.
- Manter nomes consistentes em tabelas, classes, endpoints e componentes.
- Toda lógica de negócio fica em `service/`, nunca nos controllers.
- Código limpo, sem comentários excessivos, com tratamento de erros adequado.

---

## Perfis de Usuário

| Tipo     | Descrição                                                       |
|----------|-----------------------------------------------------------------|
| CLIENT   | Contrata serviços, usa carteira, pode participar de consórcio.  |
| PROVIDER | Presta serviços, tem dashboard, portfólio e perfil público.     |
| ADMIN    | Gerencia a plataforma, media disputas, aprova cadastros.        |

---

## Roadmap de Funcionalidades (ordem de desenvolvimento)

### Fase 1 — MVP Marketplace

#### 1. Autenticação e Contas ✅ (já desenvolvida)
- Cadastro e login de CLIENT e PROVIDER.
- JWT para autenticação em todos os endpoints protegidos.
- Tabela `users` com: id, name, email, passwordHash, type, active.
- Integração futura: todos os módulos dependem desta camada de usuário.

#### 2. Perfil de Prestador e Portfólio
- Entidade `ProviderProfile` vinculada a `User` do tipo PROVIDER.
- Campos: bio, foto de perfil, localização (cidade/bairro), especialidades, anos de experiência.
- Portfólio de trabalhos: fotos + descrição de serviços já realizados.
- Avaliação média calculada dinamicamente a partir das avaliações recebidas.
- Integração: base para o módulo de busca e catálogo de serviços.

#### 3. Catálogo de Serviços e Pacotes
- Cada PROVIDER pode cadastrar serviços com: título, categoria, descrição, preço base,
  o que está incluso, o que não está incluso, tempo estimado de execução.
- Categorias de serviço predefinidas (elétrico, hidráulico, limpeza, beleza, TI, etc.)
- Preço mínimo por categoria calculado por IA (para evitar dumping de preço).
- Integração: alimenta o módulo de busca e é a base para criação de ordens de serviço.

#### 4. Busca e Descoberta
- Barra de busca por texto livre (nome do serviço ou profissão).
- Resultados mostram profissionais com aquele serviço cadastrado.
- Filtros: categoria, preço, distância, avaliação mínima.
- Ordenação multi-critério: avaliação + relevância + destaque pago + proximidade.
- Nunca ordenar apenas por menor preço (evitar leilão de preço).
- Integração: usa perfis, serviços, avaliações e futuramente anúncios patrocinados.

#### 5. Agendamento
- Cliente escolhe serviço e solicita horário(s).
- Prestador aceita ou recusa com possibilidade de contraproposta de horário.
- Estados do agendamento: PENDING, CONFIRMED, IN_PROGRESS, COMPLETED, CANCELLED.
- Integração: é o gatilho para criação da ordem de serviço e do pagamento.

#### 6. Ordens de Serviço
- Entidade central que une: CLIENT + PROVIDER + SERVICE + PRICE + SCHEDULE.
- Estados: CREATED → CONFIRMED → IN_PROGRESS → AWAITING_APPROVAL → COMPLETED / DISPUTED.
- Prestador envia confirmação de conclusão com fotos obrigatórias.
- Cliente aprova ou abre disputa.
- Integração: aciona liberação do pagamento em custódia, avaliação e dashboard.

#### 7. Pagamentos em Custódia (Escrow)
- Versão 1: cliente paga em BRL (simulação de cartão ou Pix).
- Valor fica retido na tesouraria da plataforma até a aprovação do serviço.
- Após aprovação: plataforma desconta comissão (9,99%) e libera o restante ao prestador.
- Taxa de serviço cobrada do cliente: R$ 4,99 por ordem.
- Disputas: equipe de suporte analisa fotos e decide a liberação.
- Integração futura: saldo migra para carteira digital interna em BRL Digital (stablecoin).

#### 8. Avaliações e Reputação
- Cliente avalia prestador ao final de cada ordem: nota (1–5) + comentário.
- Média de avaliação atualizada dinamicamente no perfil do prestador.
- Avaliações influenciam diretamente a posição nos resultados de busca.
- Integração: alimenta o score de reputação usado na ordenação da busca.

#### 9. Painel do Prestador (Dashboard)
- Visualização de ordens (ativas, pendentes, concluídas, canceladas).
- Faturamento: total recebido, a receber e histórico.
- Agenda: calendário de atendimentos.
- Configurações: online/offline, raio de atendimento, serviços ativos.
- Integração: consolida dados de ordens, agenda, pagamentos e avaliações.

#### 10. Painel do Cliente
- Histórico de serviços contratados e status atual.
- Prestadores favoritos.
- Formas de pagamento salvas.
- Cupons e promoções disponíveis.
- Histórico de transações financeiras.

---

### Fase 2 — Monetização Avançada

#### 11. Anúncios Patrocinados
- PROVIDER pode pagar para aparecer em destaque nos resultados de busca.
- Empresas parceiras (lojas, marcas) podem anunciar para o público da plataforma.
- Segmentação por categoria de serviço e perfil de usuário.
- Modelo de cobrança: CPC (custo por clique) ou CPM (custo por mil impressões).

#### 12. Academia de Cursos
- Prestadores podem acessar cursos para se certificar em novas categorias.
- Certificações aumentam o score de reputação e visibilidade na busca.
- Receita: cobrança por curso ou assinatura mensal (Plano Pro do Prestador).

---

### Fase 3 — Ecossistema Financeiro

#### 13. Carteira Digital (BRL)
- Saldo interno em BRL para clientes e prestadores.
- Cash-in via Pix ou cartão.
- Cash-out via Pix para conta bancária externa.
- Saldo retido na tesouraria investe automaticamente em Tesouro Selic (CDI 100%).
- Pagamento de serviços direto pelo saldo da carteira (sem taxa de serviço).
- Integração total com ordens de serviço, consórcio e módulos financeiros futuros.

#### 14. Stablecoin BRL Digital
- Token interno 1:1 lastreado em Real, respaldado por Tesouro Selic na tesouraria.
- Todo saldo da carteira pode ser convertido em BRL Digital automaticamente.
- Transações entre usuários da plataforma: gratuitas para PF (P2P).
- Transações B2B: 0,5% + R$ 0,30.
- O rendimento gerado pelos títulos públicos fica 100% na tesouraria da plataforma.
- Integração: substitui gradualmente o BRL nas ordens de serviço e consórcio.

#### 15. Consórcio
- Grupos de consórcio de imóveis (240 meses, 900 pessoas por grupo) e automóveis
  (90 meses, 900 pessoas por grupo), com faixas de crédito diferentes.
- Parcelas pagas em BRL convertem para BRL Digital e ficam na pool de liquidez global.
- Pool global rende 100% do tempo (Tesouro Selic / IPCA+).
- Rendimento da pool: parte vai para a plataforma, parte para amortizar reajuste anual
  (INCC para imóveis, IPCA/FIPE para automóveis) de contemplados.
- Contemplações pagas em BRL Digital, aceitas por lojas e construtoras parceiras.
- Lances: livre (do bolso), fixo (25%) e embutido; lances do bolso parceláveis em até 12x
  (carta fica contemplada mas liberada apenas após quitação total do lance).
- Sem taxa de administração nas cotas; receita vem do rendimento da pool e do
  desempenho do fundo.
- Comissão do vendedor: até 7% do valor do crédito, paga em 12x pela plataforma.

#### 16. Cartão de Débito e Crédito
- Cartão físico e virtual vinculado à carteira digital.
- Débito: desconta do saldo em BRL Digital.
- Crédito: limite baseado no histórico de transações dentro da plataforma.
- Cashback em BRL Digital para gastos dentro do ecossistema.

#### 17. Remessas Internacionais e Câmbio
- Envio de dinheiro para o exterior com taxa de 0,5% + $0,30.
- Câmbio mais barato que bancos tradicionais (custo operacional via blockchain).
- USD Digital disponível na carteira para proteção de patrimônio e compras no exterior.

#### 18. Empréstimos
- Crédito Fiat: baseado no score comportamental dentro da plataforma.
- Crédito Cripto: colateral em BRL Digital ou criptomoedas.
- P2P lending futuro: usuários emprestam para outros dentro da plataforma.

---

### Fase 4 — Expansão de Verticals

#### 19. Estações de Carregamento Elétrico
- Rede de carregadores para veículos elétricos.
- Pagamento exclusivo via carteira/stablecoin da plataforma.
- Dados de uso alimentam o perfil financeiro do usuário para score de crédito.

---

## Modelo de Receita (Fontes de Monetização)

| Fonte                          | Mecanismo                                              |
|-------------------------------|--------------------------------------------------------|
| Comissão de serviços           | 9,99% sobre cada serviço concluído (do prestador)      |
| Taxa de serviço                | R$ 4,99 por ordem (do cliente)                         |
| Rendimento da tesouraria       | 100% CDI sobre float de carteiras e pool do consórcio  |
| Transações B2B                 | 0,5% + R$ 0,30 por transação                           |
| Anúncios patrocinados          | CPC/CPM segmentado                                     |
| Academia de cursos             | Venda de cursos / Plano Pro do Prestador               |
| Remessas internacionais        | 0,5% + $0,30 por remessa                               |
| Spread de câmbio               | Diferencial entre taxa de mercado e taxa cobrada        |
| Juros de empréstimos           | Taxa de juros sobre crédito concedido                  |
| Desempenho do fundo consórcio  | Performance fee sobre rendimento acima do índice base  |
| Swap de stablecoin             | Spread na conversão BRL ↔ USD Digital                  |

---

## Regras de Negócio Críticas

1. **Escrow obrigatório:** nenhum pagamento é liberado sem confirmação de fotos + aprovação
   do cliente.
2. **Preço mínimo por categoria:** IA calcula e bloqueia cadastro de serviço abaixo do piso.
3. **Ordenação por múltiplos critérios:** nunca apenas preço; sempre ponderar avaliação,
   relevância e destaque pago.
4. **Lance parcelado:** carta de crédito do consórcio fica contemplada mas bloqueada até
   quitação total do lance parcelado.
5. **Reajuste do consórcio:** anual, pelo INCC (imóveis) ou IPCA/FIPE (auto), incide nas
   parcelas a vencer e aumenta o valor total da carta de crédito.
6. **BRL Digital sempre lastreado 1:1:** a tesouraria deve manter cobertura total em
   títulos públicos.
7. **P2P gratuito para PF:** transferências entre pessoas físicas dentro da plataforma são
   sem custo para maximizar retenção.

---

## Integrações Externas Previstas

- **Pix (Banco Central):** cash-in e cash-out da carteira digital.
- **Processadora de cartões:** adquirência para pagamentos com cartão de crédito/débito.
- **Tesouro Direto / Custodiante:** aplicação automática do float da tesouraria.
- **Bancos parceiros:** saques da carta de crédito do consórcio e liquidação de remessas.
- **Firebase / OneSignal:** notificações push para agendamentos, ordens e pagamentos.
- **AWS S3:** armazenamento de fotos de portfólio, serviços concluídos e documentos KYC.

---

## Estrutura de Pastas Backend (Spring Boot)

src/main/java/com/mechama/
├── MeChamaApplication.java
├── config/
│ └── SecurityConfig.java
├── model/
│ ├── User.java
│ ├── ProviderProfile.java
│ ├── Service.java
│ ├── Order.java
│ ├── Payment.java
│ ├── Review.java
│ └── enums/
│ ├── UserType.java
│ └── OrderStatus.java
├── repository/
├── dto/
├── service/
└── controller/

text

## Estrutura de Pastas Mobile (React Native)

src/
├── api/
│ ├── auth.ts
│ ├── services.ts
│ ├── orders.ts
│ └── wallet.ts
├── screens/
│ ├── Auth/
│ ├── Home/
│ ├── Search/
│ ├── Provider/
│ ├── Orders/
│ ├── Wallet/
│ └── Consortium/
├── components/
├── navigation/
├── store/
└── utils/

text

---

## Notas para o Claude Code

- Sempre que criar uma nova entidade, explicar como ela se relaciona com as já existentes.
- Sempre gerar a migration SQL junto com a entidade JPA.
- Sempre gerar os DTOs de request e response separados da entidade.
- Nunca expor o `passwordHash` em nenhum response.
- O token JWT deve conter: `userId`, `email` e `type` nos claims.
- Cada funcionalidade nova deve terminar com a sugestão da próxima funcionalidade lógica.
- Manter consistência total com os nomes já definidos neste documento.
Cole esse arquivo no campo "Instruções" do projeto no Claude Code (exatamente como você está fazendo na imagem que enviou) e o modelo terá contexto completo do negócio, da stack e da ordem de desenvolvimento. Quer que eu já escreva a Tarefa 2 (Perfil de Prestador e Portfólio) no mesmo formato detalhado?

Preparado usando Claude Sonnet 4.6 Thinking
Quero que tenha todas as instruções necessárias para um coding bom de verdade
Concluiu 1 etapa
Segue o documento completo, atualizado com todas as instruções de engenharia de software de nível produção. Salve como MECHAMA_CONTEXT.md na raiz do projeto.

text
# MeChamaAPP — Documento Mestre de Contexto e Padrões de Engenharia

---

## 1. Visão Geral do Produto

O MeChamaAPP é um super-app mobile-first que conecta clientes a prestadores de serviços,
com ecossistema financeiro próprio integrado (carteira digital, stablecoin, consórcio).

Compete diretamente com:
- GetNinjas, Uber Serviços (marketplace)
- iFood (modelo de plataforma e monetização)
- Nubank, BTG Pactual, XP Investimentos (financeiro, no futuro)

A premissa financeira central: todo dinheiro que circula na plataforma deve ficar retido
no ecossistema o máximo de tempo possível, gerando rendimento para a tesouraria via
Tesouro Selic / IPCA+.

---

## 2. Stack Tecnológica

| Camada              | Tecnologia Principal               | Observações                              |
|---------------------|------------------------------------|------------------------------------------|
| Backend             | Java 21 + Spring Boot 3.x          | Monolito modular → microsserviços futuro |
| Banco relacional    | PostgreSQL 15+                     | Principal fonte de verdade               |
| Cache               | Redis                              | Sessões, rate limit, filas leves         |
| Mensageria          | RabbitMQ (futuro: Kafka)           | Eventos assíncronos                      |
| Mobile              | React Native (Expo managed)        | iOS + Android com um código              |
| Web (painéis)       | Next.js 14+ (App Router)           | Dashboard admin e prestador              |
| Auth                | JWT (access token + refresh token) | Stateless                                |
| Storage de arquivos | AWS S3 (ou compatível)             | Fotos, documentos, portfólio             |
| Notificações        | Firebase Cloud Messaging (FCM)     | Push nativo iOS e Android                |
| Cloud               | AWS (ou GCP)                       | Início com EC2/RDS, depois ECS/RDS       |
| Containers          | Docker + Docker Compose (dev)      | Kubernetes no futuro                     |
| CI/CD               | GitHub Actions                     | Build, test, deploy automático           |

---

## 3. Princípios Gerais de Desenvolvimento

### 3.1 Modo de Trabalho
- Desenvolver **uma funcionalidade por vez**, completamente (model → migration → repo →
  service → controller → DTOs → telas → testes), antes de passar para a próxima.
- Antes de iniciar cada funcionalidade, apresentar:
  1. Resumo do que ela faz.
  2. Como se conecta com o que já existe (dependências).
  3. Impacto em tabelas/entidades já criadas.
- Nunca reescrever código já funcional; sempre evoluir a arquitetura existente.
- Ao final de cada funcionalidade, sugerir a próxima lógica no roadmap.

### 3.2 Arquitetura
- Monolito modular no início: separar por pacotes de domínio, não por tipo técnico.
- Estrutura de pacotes:
com.mechama.
├── user/ # autenticação, cadastro, perfis
├── service/ # catálogo de serviços dos prestadores
├── order/ # ordens de serviço
├── payment/ # pagamentos, escrow, carteira
├── review/ # avaliações
├── search/ # busca e descoberta
├── notification/ # push e emails
├── consortium/ # consórcio (fase 3)
├── wallet/ # carteira digital (fase 3)
└── shared/ # utilitários, exceções, config, base classes

text
- Cada módulo tem suas próprias camadas internas:
`model/ → repository/ → dto/ → service/ → controller/`
- Toda lógica de negócio fica em `service/`. Controllers só recebem, validam e delegam.
- Repositórios só fazem acesso a dados. Nunca lógica de negócio em repositório.

### 3.3 Regras de Código Sempre Aplicadas
- Usar **Lombok** para reduzir boilerplate (`@Getter`, `@Builder`, `@RequiredArgsConstructor`).
- Usar `record` do Java para DTOs imutáveis onde possível.
- Nunca expor entidades JPA diretamente no response da API. Sempre usar DTOs.
- Nunca retornar `null`; usar `Optional<>` em repositórios e tratar adequadamente.
- Sempre validar entrada com `jakarta.validation` (`@NotBlank`, `@Email`, `@Min`, etc.).
- Toda exception personalizada deve estender `RuntimeException` e ter um handler global.
- Sem `System.out.println`; usar `@Slf4j` com log estruturado (`log.info`, `log.error`).
- Sem hardcode de valores de negócio no código; usar `application.yml` ou constantes.
- Sem comentários óbvios; o código deve ser autoexplicativo. Comentar apenas decisões
não óbvias de arquitetura ou regras de negócio complexas.

---

## 4. Padrões de API REST

### 4.1 Convenções de URL
POST /api/v1/{recurso} → criar
GET /api/v1/{recurso} → listar
GET /api/v1/{recurso}/{id} → buscar por id
PUT /api/v1/{recurso}/{id} → atualizar completo
PATCH /api/v1/{recurso}/{id} → atualizar parcial
DELETE /api/v1/{recurso}/{id} → remover
GET /api/v1/{recurso}/{id}/{sub} → sub-recurso

text

Exemplos reais:
POST /api/v1/auth/register
POST /api/v1/auth/login
GET /api/v1/providers/{id}/profile
GET /api/v1/providers/{id}/services
POST /api/v1/orders
PATCH /api/v1/orders/{id}/status
GET /api/v1/orders/{id}/reviews

text

### 4.2 Formato de Response padrão

**Sucesso:**
```json
{
  "success": true,
  "data": { },
  "timestamp": "2026-03-22T15:00:00Z"
}
Erro:

json
{
  "success": false,
  "error": {
    "code": "USER_NOT_FOUND",
    "message": "Usuário não encontrado",
    "details": []
  },
  "timestamp": "2026-03-22T15:00:00Z"
}
4.3 HTTP Status Codes
text
200 OK             → listagens, updates bem-sucedidos
201 Created        → criação bem-sucedida
204 No Content     → deleção bem-sucedida
400 Bad Request    → validação de input falhou
401 Unauthorized   → token ausente ou inválido
403 Forbidden      → autenticado mas sem permissão
404 Not Found      → recurso não encontrado
409 Conflict       → conflito de dados (ex: email já existe)
422 Unprocessable  → regra de negócio violada
500 Server Error   → erro interno inesperado
4.4 Versionamento de API
Sempre prefixar com /api/v1/. Quando houver breaking changes, criar /api/v2/.

Nunca remover endpoints de versões anteriores sem deprecation e aviso.

5. Banco de Dados e Migrations
5.1 Regras para o Banco
Toda tabela deve ter:

id BIGSERIAL PRIMARY KEY

created_at TIMESTAMP NOT NULL DEFAULT NOW()

updated_at TIMESTAMP NOT NULL DEFAULT NOW()

Usar snake_case para tabelas e colunas no PostgreSQL.

Usar PascalCase para entidades Java e camelCase para campos Java.

Toda FK deve ter índice criado explicitamente.

Nunca usar ddl-auto: create ou update em produção. Usar Flyway para migrations.

Nomenclatura de migrations: V{numero}__{descricao_com_underscores}.sql
Exemplo: V001__create_users_table.sql, V002__create_provider_profiles.sql

5.2 Exemplo de Migration padrão
sql
-- V001__create_users_table.sql
CREATE TABLE users (
    id           BIGSERIAL PRIMARY KEY,
    name         VARCHAR(150)  NOT NULL,
    email        VARCHAR(255)  NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    type         VARCHAR(20)   NOT NULL CHECK (type IN ('CLIENT', 'PROVIDER', 'ADMIN')),
    active       BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_type  ON users(type);
5.3 Configuração Flyway no application.yml
text
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true
  jpa:
    hibernate:
      ddl-auto: validate  # nunca create ou update em staging/prod
6. Segurança
6.1 Autenticação JWT
Access token: expiração de 15 minutos.

Refresh token: expiração de 30 dias, salvo em banco de dados (tabela refresh_tokens).

Claims obrigatórios no JWT: sub (userId), email, type, iat, exp.

Nunca salvar JWT no LocalStorage do mobile; usar SecureStore (Expo) ou Keychain.

Rotacionar refresh token a cada uso (one-time use).

6.2 Autorização
Usar @PreAuthorize do Spring Security para controle por papel (role).

Regras gerais:

Apenas PROVIDER pode criar/editar serviços.

Apenas CLIENT pode criar ordens.

Apenas ADMIN pode suspender contas e acessar relatórios globais.

Cada usuário só acessa seus próprios dados (verificar userId do token vs. recurso).

6.3 Proteções Obrigatórias
Rate limiting: no máximo 10 tentativas de login por IP por minuto (usar Redis).

Senhas: sempre hash com BCrypt, custo mínimo 10.

Inputs: sanitizar tudo antes de persistir; nunca confiar no cliente.

SQL Injection: nunca usar queries com concatenação de string; sempre usar JPA/JPQL
com parâmetros nomeados ou @Query com :param.

CORS: configurar whitelist de origens permitidas no SecurityConfig.

Arquivos upload: validar tipo MIME e tamanho máximo (ex: 5MB por foto).

7. Tratamento de Erros
7.1 Exception Handler Global
Sempre criar um @RestControllerAdvice centralizado:

java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse.of("NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException ex) {
        log.warn("Business rule violation: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
            .body(ErrorResponse.of(ex.getCode(), ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors()
            .stream()
            .map(f -> f.getField() + ": " + f.getDefaultMessage())
            .toList();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse.ofDetails("VALIDATION_ERROR", "Dados inválidos", errors));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse.of("INTERNAL_ERROR", "Erro interno no servidor"));
    }
}
7.2 Exceptions personalizadas obrigatórias
java
// Quando recurso não é encontrado no banco
public class ResourceNotFoundException extends RuntimeException { ... }

// Quando regra de negócio é violada
public class BusinessException extends RuntimeException {
    private final String code;
    ...
}

// Quando usuário não tem permissão para o recurso
public class ForbiddenException extends RuntimeException { ... }
8. Padrões de Testes
8.1 O que testar em cada funcionalidade
Testes unitários (src/test/): toda classe service/ deve ter cobertura mínima de 80%.

Testes de integração: endpoints críticos testados com @SpringBootTest +
MockMvc ou WebTestClient.

Testes de contrato: para módulos de pagamento e carteira.

8.2 Nomenclatura de testes
java
// Formato: should{Comportamento}When{Condição}
@Test
void shouldReturnTokenWhenCredentialsAreValid() { ... }

@Test
void shouldThrowExceptionWhenEmailAlreadyExists() { ... }
8.3 Ferramentas de teste
JUnit 5 + Mockito para unitários.

@DataJpaTest para repositórios (usa H2 em memória).

Testcontainers para testes de integração com PostgreSQL real.

Jest + React Native Testing Library para testes mobile.

9. Performance e Escalabilidade
9.1 Banco de Dados
Toda query de listagem deve ter paginação (Pageable no Spring Data).

Nunca usar findAll() sem paginação em tabelas que podem crescer.

Usar @EntityGraph ou JOIN FETCH para evitar N+1 queries.

Campos usados em filtros e buscas (email, type, status, FK) devem ter índices.

Para buscas textuais (nome de serviço), usar ILIKE ou, no futuro, Elasticsearch.

9.2 Cache com Redis
Cachear resultados de busca frequentes (categorias, serviços populares).

Cachear perfis de prestadores mais acessados (TTL 5 minutos).

Nunca cachear dados financeiros (saldo, pagamentos).

Padrão de chave: mechama:{modulo}:{identificador}
Exemplo: mechama:provider:123:profile

9.3 Processamento Assíncrono
Notificações push: sempre assíncronas via fila (nunca bloquear o request do usuário).

Emails transacionais: fila com retry automático.

Cálculo de score/ranking de prestadores: job periódico, não em tempo real.

10. Padrões Mobile (React Native)
10.1 Estrutura de Pastas
text
src/
├── api/            # funções de chamada à API (axios)
├── screens/        # telas organizadas por domínio
├── components/     # componentes reutilizáveis
├── navigation/     # React Navigation (Stack, Tab, Drawer)
├── store/          # estado global (Zustand ou Redux Toolkit)
├── hooks/          # custom hooks
├── utils/          # formatadores, validadores, constantes
├── types/          # TypeScript types e interfaces
└── theme/          # cores, fontes, espaçamentos
10.2 Regras de Código Mobile
Usar TypeScript estrito (strict: true no tsconfig).

Nenhuma tela deve ter lógica de negócio; usar custom hooks (useOrders, useAuth).

Toda chamada de API dentro de hook ou serviço, nunca diretamente na tela.

Tratar sempre os estados: loading, error, empty e data em cada listagem.

Usar react-query (TanStack Query) para gerenciar cache e estados de requisição.

Imagens e fotos: comprimir antes do upload no mobile (usar expo-image-manipulator).

Tokens JWT: armazenar com expo-secure-store, nunca AsyncStorage.

Interceptor Axios: adicionar token JWT automaticamente em todas as requests protegidas
e tratar refresh automático no 401.

10.3 Navegação
text
RootNavigator
├── AuthStack (não autenticado)
│   ├── LoginScreen
│   ├── RegisterScreen
│   └── OnboardingScreen
└── AppTabs (autenticado)
    ├── HomeTab
    │   └── HomeScreen, SearchScreen, ServiceDetailScreen
    ├── OrdersTab
    │   └── OrdersListScreen, OrderDetailScreen
    ├── WalletTab (fase 3)
    │   └── WalletScreen, TransactionHistoryScreen
    └── ProfileTab
        └── ProfileScreen, SettingsScreen
11. Variáveis de Ambiente
Backend (application.yml com profiles)
text
# application-dev.yml
spring:
  datasource:
    url: ${DB_URL:jdbc:postgresql://localhost:5432/mechama_dev}
    username: ${DB_USER:mechama}
    password: ${DB_PASS:mechama123}
  redis:
    host: ${REDIS_HOST:localhost}
    port: 6379

jwt:
  secret: ${JWT_SECRET}
  access-token-expiration-ms: 900000      # 15 minutos
  refresh-token-expiration-ms: 2592000000 # 30 dias

aws:
  s3:
    bucket: ${S3_BUCKET}
    region: ${AWS_REGION:us-east-1}

firebase:
  credentials-path: ${FIREBASE_CREDENTIALS_PATH}
Mobile (.env)
text
API_BASE_URL=http://localhost:8080/api/v1
FIREBASE_PROJECT_ID=mechama-app
SENTRY_DSN=
12. Docker e Ambiente de Desenvolvimento
docker-compose.yml
text
version: '3.9'
services:
  postgres:
    image: postgres:15
    environment:
      POSTGRES_DB: mechama_dev
      POSTGRES_USER: mechama
      POSTGRES_PASSWORD: mechama123
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"

  rabbitmq:
    image: rabbitmq:3-management
    ports:
      - "5672:5672"
      - "15672:15672" # painel de gerenciamento

volumes:
  postgres_data:
Comando para subir o ambiente:

bash
docker-compose up -d
13. Perfis de Usuário e Papéis
Tipo	Pode fazer
CLIENT	Buscar serviços, contratar, pagar, avaliar, usar carteira, participar consórcio
PROVIDER	Criar perfil, cadastrar serviços, aceitar ordens, receber pagamentos, dashboard
ADMIN	Tudo + suspender contas, mediar disputas, acessar relatórios, configurar preços
14. Roadmap de Funcionalidades (ordem de desenvolvimento)
Fase 1 — MVP Marketplace
1. Autenticação e Contas ✅
Cadastro/login CLIENT e PROVIDER com JWT.

Tabela users: id, name, email, password_hash, type, active, created_at, updated_at.

Endpoints: POST /api/v1/auth/register, POST /api/v1/auth/login,
POST /api/v1/auth/refresh, POST /api/v1/auth/logout.

Integração: base de todos os módulos. Todos os endpoints protegidos usam o userId
extraído do JWT.

2. Perfil de Prestador e Portfólio
Entidade ProviderProfile vinculada 1:1 a User (type = PROVIDER).

Campos: bio, avatar_url, city, neighborhood, experience_years, specialties (array),
avg_rating (calculado), total_reviews, active.

Portfólio: entidade PortfolioItem (title, description, photo_url, created_at).

Endpoints: GET/PUT /api/v1/providers/{id}/profile,
POST/DELETE /api/v1/providers/{id}/portfolio.

Integração: perfil é exibido nos resultados de busca e na tela de detalhe do prestador.

3. Catálogo de Serviços e Pacotes
Entidade ServiceOffering: title, description, category, base_price, duration_minutes,
what_is_included, what_is_not_included, active, provider_id (FK).

Entidade ServiceCategory: name, icon, min_price (piso calculado por IA).

Validação: base_price >= category.min_price (bloqueia dumping de preço).

Endpoints: CRUD completo em /api/v1/providers/{id}/services.

Integração: serviços são a unidade de busca, filtragem e criação de ordens.

4. Busca e Descoberta
Endpoint: GET /api/v1/search?q={query}&category={cat}&city={city}&minRating={r}&page={p}.

Retorna prestadores com serviços que correspondem à busca.

Score de ordenação: (avg_rating * 0.4) + (relevancia_texto * 0.3) + (destaque_pago * 0.2) + (proximidade * 0.1).

Paginação obrigatória (máx. 20 resultados por página).

Integração: usa ProviderProfile, ServiceOffering e futuramente Ad.

5. Agendamento
Entidade Schedule: provider_id, date, start_time, end_time, available (bool).

Entidade AppointmentRequest: client_id, provider_id, service_id, proposed_datetime,
status (PENDING, CONFIRMED, REJECTED, COUNTER_PROPOSED), counter_datetime.

Endpoints: prestador gerencia disponibilidade; cliente solicita horário.

Integração: agendamento confirmado gera uma Order.

6. Ordens de Serviço
Entidade Order: client_id, provider_id, service_id, price, status, scheduled_at,
completion_photos (array), client_approved_at, disputed_at.

Status: CREATED → CONFIRMED → IN_PROGRESS → AWAITING_APPROVAL → COMPLETED / DISPUTED.

Regras: prestador envia fotos ao concluir; cliente tem 48h para aprovar ou disputar.

Endpoints: POST /api/v1/orders, PATCH /api/v1/orders/{id}/status,
POST /api/v1/orders/{id}/completion, POST /api/v1/orders/{id}/approve.

Integração: conclusão aprovada aciona liberação do escrow (pagamento).

7. Pagamentos em Custódia (Escrow)
Entidade Payment: order_id, amount, platform_fee, provider_amount, status,
payment_method, paid_at, released_at.

Status: PENDING → PAID → IN_ESCROW → RELEASED / REFUNDED.

Taxas: plataforma retém 9,99% + R$ 4,99 (taxa do cliente separada na ordem).

Regra: liberação automática após aprovação do cliente; reembolso automático se disputa
for favorável ao cliente.

Integração futura: saldo do prestador vai para a carteira digital interna da plataforma.

8. Avaliações e Reputação
Entidade Review: order_id, client_id, provider_id, rating (1-5), comment, created_at.

Restrição: apenas 1 review por ordem; só quem concluiu pode avaliar.

Atualizar avg_rating e total_reviews em ProviderProfile após cada review.

Endpoint: POST /api/v1/orders/{id}/review, GET /api/v1/providers/{id}/reviews.

Integração: avg_rating alimenta o score de busca do prestador.

9. Painel do Prestador (Dashboard)
Endpoints agregados: resumo financeiro, ordens por status, calendário, métricas.

GET /api/v1/providers/me/dashboard: total recebido, a receber, ordens ativas,
média de avaliação, visualizações do perfil.

Integração: consolida Orders, Payments, Reviews e Schedule.

10. Painel do Cliente
GET /api/v1/clients/me/orders: histórico com filtro por status.

GET /api/v1/clients/me/favorites: prestadores favoritados.

Integração: consolida Orders e ProviderProfile.

Fase 2 — Monetização
11. Anúncios Patrocinados
Entidade Ad: advertiser_id, type (PROVIDER_HIGHLIGHT, BRAND), category_target,
cpc, cpm, budget, active, starts_at, ends_at.

Lógica: ads com orçamento disponível entram na pool de destaques da busca.

Cobrança por clique ou impressão (job periódico de faturamento).

12. Academia de Cursos
Entidade Course: title, description, category, price, video_url, certificate_template.

Entidade Enrollment: provider_id, course_id, progress, completed_at.

Certificação concluída adiciona badge ao perfil do prestador.

Fase 3 — Ecossistema Financeiro
13. Carteira Digital (BRL)
Entidade Wallet: user_id, balance, locked_balance, currency (BRL).

Entidade WalletTransaction: wallet_id, type, amount, description, reference_id, created_at.

Cash-in via Pix (webhook do PSP) e cartão.

Cash-out via Pix com validação de chave.

Saldo total da tesouraria aplicado em Tesouro Selic (job diário de conciliação).

Regra crítica: balance nunca pode ficar negativo; usar transações no banco com lock.

14. Stablecoin BRL Digital
Token interno 1:1 lastreado em Real.

Conversão automática do saldo da carteira em BRL Digital.

P2P entre usuários da plataforma: gratuito para PF.

B2B: 0,5% + R$ 0,30 por transação.

Rendimento da tesouraria: 100% da plataforma.

15. Consórcio
Grupos de imóveis: 240 meses, 900 pessoas, faixas de crédito variadas.

Grupos de automóveis: 90 meses, 900 pessoas, faixas variadas.

Parcelas em BRL → convertidas em BRL Digital → pool global rende 24/7.

Lances: livre (bolso), fixo 25%, embutido.

Lance do bolso parcelável em 12x: carta contemplada mas bloqueada até quitação.

Reajuste anual: INCC (imóveis), IPCA/FIPE (auto); amortizado pelo rendimento da pool.

Sem taxa de administração nas cotas; receita vem de performance fee do fundo.

15. Modelo de Receita
Fonte	Mecanismo
Comissão marketplace	9,99% por serviço concluído (do prestador)
Taxa de serviço	R$ 4,99 por ordem (do cliente)
Rendimento de tesouraria	100% CDI sobre float de carteiras e pool do consórcio
Transações B2B stablecoin	0,5% + R$ 0,30 por transação
Anúncios patrocinados	CPC/CPM segmentado por categoria e perfil
Academia de cursos	Venda avulsa + assinatura Pro do Prestador
Remessas internacionais	0,5% + $0,30 por operação
Spread cambial	Diferencial BRL ↔ USD Digital
Empréstimos	Juros sobre crédito Fiat e cripto
Performance fee do consórcio	% sobre rendimento acima do índice base
16. Regras de Negócio Críticas (Imutáveis)
Nenhum pagamento liberado sem confirmação de fotos + aprovação do cliente.

Preço mínimo por categoria: IA calcula o piso; backend rejeita cadastro abaixo.

Ordenação de busca: nunca apenas por preço; sempre score multi-critério.

Lance parcelado: carta contemplada mas bloqueada até quitação 100% do lance.

Reajuste do consórcio: anual, INCC ou IPCA/FIPE, só nas parcelas a vencer.

BRL Digital lastreado 1:1: tesouraria deve cobrir 100% do saldo emitido.

P2P PF gratuito: transferências entre pessoas físicas sem custo nenhum.

Senha: nunca salvar em texto plano; sempre BCrypt custo ≥ 10.

JWT: nunca salvar no AsyncStorage; sempre Secure Store.

Dados financeiros: nunca cachear; sempre consulta direta ao banco com transação.

17. Integrações Externas
Sistema	Finalidade
Pix (Banco Central)	Cash-in e cash-out da carteira digital
PSP (ex: Stripe/Pagar.me)	Processamento de cartão de crédito/débito
AWS S3	Fotos de portfólio, conclusão de serviço, documentos KYC
Firebase FCM	Push notifications iOS e Android
Tesouro Direto/Custodiante	Aplicação automática do float da tesouraria
SendGrid / SES	Emails transacionais (confirmação, reset de senha)
Sentry	Monitoramento de erros em produção
Datadog / Grafana	Métricas de performance e uptime
18. Notas Finais para o Claude Code
Sempre que criar uma entidade, gerar a migration SQL correspondente.

Sempre criar DTOs separados (Request e Response) para cada entidade.

Nunca expor passwordHash, tokens ou dados sensíveis em nenhum DTO de response.

O token JWT deve conter nos claims: userId, email, type.

Toda listagem deve ter paginação com Pageable e retornar Page<T>.

Para operações financeiras (débito, crédito, escrow): usar @Transactional e
SELECT ... FOR UPDATE para evitar race conditions.

Ao criar um módulo novo, verificar se afeta algum módulo existente e atualizar
os pontos de integração necessários.

Manter 100% de consistência de nomenclatura com este documento.

Código mobile em TypeScript estrito; nunca usar any.

Ao final de cada funcionalidade: listar o que foi criado, o que foi modificado
em módulos existentes, e qual a próxima funcionalidade sugerida.

text

***

Esse `.md` cobre engenharia de ponta a ponta: arquitetura, segurança, banco, testes, performance, mobile e negócio. 