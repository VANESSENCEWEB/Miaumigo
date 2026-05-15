# Repository Guidelines

## Project Structure & Module Organization

This repository is the backend for **Gerenciador de Lares Adotivos**, a Java 21/Spring Boot 3 API for managing foster homes, adoptable animals, adopters, JWT auth, AI-generated adoption copy, and email delivery. The current source root is `src/main/java/com/Miaumigo/Miaumigo`; new production code should follow the planned package layout: `config`, `controller`, `service`, `repository`, `domain`, `dto`, `exception`, and `security`. Tests mirror production packages under `src/test/java`. Keep static files in `src/main/resources/static`, templates in `src/main/resources/templates`, and application configuration in `src/main/resources/application.properties`.

## Domain & API Scope

Core entities are `Animal`, `Adotante`, `Lar`, and `Usuario`, using UUID primary keys. Required enums include `Especie` (`CACHORRO`, `GATO`, `OUTRO`), `Porte` (`PEQUENO`, `MEDIO`, `GRANDE`), `AnimalStatus` (`DISPONIVEL`, `EM_PROCESSO`, `ADOTADO`), and `Role` (`ADMIN`, `OPERADOR`). All API routes use `/api/v1`; only `/auth/**` is public. Implement endpoints from the spec for auth, animals, adopters, homes, and `POST /animais/{id}/divulgar`.

## Build, Test, and Development Commands

Use the Maven wrapper:

```sh
./mvnw spring-boot:run
./mvnw test
./mvnw clean package
```

`spring-boot:run` starts the API, `test` runs JUnit tests, and `clean package` builds the artifact in `target/`.

## Coding Style & Naming Conventions

Use Java 21 and Spring Boot conventions. Keep Java indentation consistent with the existing files: tabs for nested blocks. Classes use `PascalCase`; methods, fields, and DTO properties use `camelCase`. Prefer Portuguese domain names matching the API (`AnimalService`, `LarController`, `deveRetornarAnimal_quandoIdValido`). Keep JSON fields in the contract style, such as `lar_id`, `criado_em`, and `expira_em`, mapping them through DTOs when needed.

## Testing Guidelines

TDD is mandatory: write the failing test first, implement the minimum, then refactor. Use JUnit 5 and Mockito for services, MockMvc for controllers, and H2 or Testcontainers for repository/integration tests. Test method names should follow `deve<Comportamento>_quando<Condicao>()`, for example `deveLancarExcecao_quandoAnimalNaoEncontrado()`. For each feature, start with service tests, then service code, then controller tests, then controller code. Run `./mvnw test` before every PR.

## Security & Configuration

Use Spring Security with JWT bearer auth. `ADMIN` has full access; `OPERADOR` manages animals from its own `Lar`, can trigger divulgacao, and can read adopters. Do not commit secrets. Configure via environment variables: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `JWT_EXPIRATION_MS`, `ANTHROPIC_API_KEY`, `ANTHROPIC_MODEL`, `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, and `MAIL_PASSWORD`.

## AI & Email Integration

`DivulgacaoService` orchestrates animal lookup, availability validation, prompt creation, `IAService` Anthropic calls, and `EmailService` delivery. The generated adoption text must be empathetic, max three paragraphs, and must not expose IDs or internal fields. Return `502` for AI failures and `503` for email failures.

## Commit & Pull Request Guidelines

Recent history has short Portuguese commits; use clearer imperative messages going forward, such as `Adiciona CRUD de animais`. PRs must include scope, endpoints changed, tests run, linked issue when available, and request/response examples for API changes.
