# API Miaumigo - Mapa Atual

Contrato implementado para o MVP de adocao: o adotante encontra animais recomendados,
solicita adocao e acompanha se a solicitacao foi aprovada, rejeitada, cancelada ou se
ainda esta pendente.

## Estado Atual Da Autenticacao

A API possui login JWT para adotantes e operadores. As rotas protegidas usam:

```http
Authorization: Bearer <access_token>
```

Novos cadastros de adotante e operador salvam senha com BCrypt. Registros antigos que
tenham senha em texto puro ainda sao aceitos pelo login por compatibilidade.

## Fluxo Do Adotante Implementado Hoje

1. O adotante se cadastra com `POST /api/v1/adotantes`.
2. O adotante faz login com `POST /api/v1/auth/login`.
3. A resposta do login retorna `access_token`; esse token deve ser enviado como
   `Authorization: Bearer <access_token>` nas rotas protegidas do adotante.
4. O adotante consulta `GET /api/v1/adotantes/me/animais-recomendados` para ver animais
   disponiveis ordenados por compatibilidade.
5. O adotante pode abrir `GET /api/v1/animais/{id}` para ver detalhes publicos do animal.
6. O adotante cria a solicitacao com `POST /api/v1/animais/{id}/solicitacoes`, sem corpo.
7. O adotante acompanha o resultado em `GET /api/v1/adotantes/me/solicitacoes`.
8. Enquanto a solicitacao estiver `PENDENTE`, o adotante pode cancelar com
   `POST /api/v1/solicitacoes/{id}/cancelamento`.

```text
Fazer login como adotante -> receber JWT -> usar Bearer token no fluxo do adotante.
```

## Endpoints

| Metodo | Rota | Identidade atual | Uso |
| --- | --- | --- | --- |
| `POST` | `/api/v1/auth/login` | Publico | Autentica adotante ou operador e retorna JWT |
| `POST` | `/api/v1/adotantes` | Publico | Cadastra adotante |
| `POST` | `/api/v1/lares/cadastro` | Publico | Cadastra lar e primeiro operador responsavel |
| `GET` | `/api/v1/adotantes/me/animais-recomendados` | Bearer token de adotante | Lista animais disponiveis ranqueados |
| `GET` | `/api/v1/adotantes/me/solicitacoes` | Bearer token de adotante | Acompanha solicitacoes proprias |
| `POST` | `/api/v1/lares` | Bearer token de admin | Cadastra lar sem operador |
| `POST` | `/api/v1/lares/{id}/operadores` | Bearer token de admin | Cadastra operador em um lar existente |
| `GET` | `/api/v1/lares/me/solicitacoes?status=PENDENTE` | Bearer token de operador | Lista solicitacoes do lar |
| `POST` | `/api/v1/animais` | Bearer token de operador | Cadastra animal no lar do operador |
| `GET` | `/api/v1/animais?status=DISPONIVEL` | Publico | Lista animais disponiveis |
| `GET` | `/api/v1/animais/{id}` | Publico | Exibe detalhe publico do animal |
| `POST` | `/api/v1/animais/{id}/solicitacoes` | Bearer token de adotante | Cria solicitacao pendente |
| `POST` | `/api/v1/animais/{id}/texto-divulgacao` | Bearer token de operador ou admin | Gera texto de divulgacao por IA |
| `POST` | `/api/v1/solicitacoes/{id}/cancelamento` | Bearer token de adotante | Cancela solicitacao propria pendente |
| `POST` | `/api/v1/solicitacoes/{id}/aprovacao` | Bearer token de operador | Aprova solicitacao pendente |
| `POST` | `/api/v1/solicitacoes/{id}/rejeicao` | Bearer token de operador | Rejeita solicitacao pendente |

## Requests E Responses Do Adotante

### Fazer login

```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "email": "maria@email.com",
  "senha": "senha123"
}
```

Resposta `200`:

```json
{
  "access_token": "eyJhbGciOiJIUzI1NiJ9...",
  "token_type": "Bearer",
  "expira_em": "2026-05-27T12:00:00Z",
  "usuario": {
    "id": "22222222-2222-2222-2222-222222222222",
    "nome": "Maria Silva",
    "email": "maria@email.com",
    "papel": "ADOTANTE"
  }
}
```

Credenciais invalidas retornam `401`. Para adotantes, `usuario.papel` vem como `ADOTANTE`.
Para operadores, `usuario.papel` vem como `OPERADOR`.

### Cadastrar lar com responsavel

```http
POST /api/v1/lares/cadastro
Content-Type: application/json

{
  "nome": "Lar Amigo",
  "operador": {
    "nome": "Ana",
    "endereco": "Rua A, 10",
    "email": "ana@lar.com",
    "senha": "senha123",
    "cpf": "12345678901"
  }
}
```

Resposta `201`:

```json
{
  "lar": {
    "id": "55555555-5555-5555-5555-555555555555",
    "nome": "Lar Amigo"
  },
  "operador": {
    "id": "33333333-3333-3333-3333-333333333333",
    "nome": "Ana",
    "email": "ana@lar.com",
    "role": "OPERADOR",
    "lar_id": "55555555-5555-5555-5555-555555555555"
  }
}
```

Depois do cadastro, o responsavel faz login em `POST /api/v1/auth/login` e usa o token
de operador nas rotas do lar.

### Cadastrar adotante

```http
POST /api/v1/adotantes
Content-Type: application/json

{
  "nome": "Maria Silva",
  "endereco": "Rua das Flores, 123",
  "email": "maria@email.com",
  "senha": "senha123",
  "cpf": "12345678901",
  "preferencias": ["CALMO", "CARINHOSO", "CONVIVE_COM_GATOS"]
}
```

Resposta `201`:

```json
{
  "id": "22222222-2222-2222-2222-222222222222",
  "nome": "Maria Silva",
  "endereco": "Rua das Flores, 123",
  "email": "maria@email.com",
  "cpf": "12345678901",
  "preferencias": ["CALMO", "CARINHOSO", "CONVIVE_COM_GATOS"]
}
```

### Listar animais recomendados

```http
GET /api/v1/adotantes/me/animais-recomendados
Authorization: Bearer <access_token>
```

Resposta `200`:

```json
[
  {
    "id": "11111111-1111-1111-1111-111111111111",
    "nome": "Luna",
    "idade": 2,
    "porte": "PEQUENO",
    "especie": "GATO",
    "tags": ["CALMO", "CARINHOSO"],
    "cloudinary_public_id": "animais/luna",
    "compatibilidade": 2
  }
]
```

### Listar animais disponiveis

```http
GET /api/v1/animais
GET /api/v1/animais?status=DISPONIVEL
```

Resposta `200`:

```json
[
  {
    "id": "11111111-1111-1111-1111-111111111111",
    "nome": "Luna",
    "idade": 2,
    "porte": "PEQUENO",
    "especie": "GATO",
    "descricao": "Gata calma e carinhosa.",
    "status": "DISPONIVEL",
    "tags": ["CALMO", "CARINHOSO"],
    "cloudinary_public_id": "animais/luna"
  }
]
```

### Ver detalhe do animal

```http
GET /api/v1/animais/11111111-1111-1111-1111-111111111111
```

Resposta `200`:

```json
{
  "id": "11111111-1111-1111-1111-111111111111",
  "nome": "Luna",
  "idade": 2,
  "porte": "PEQUENO",
  "especie": "GATO",
  "descricao": "Gata calma e carinhosa.",
  "status": "DISPONIVEL",
  "tags": ["CALMO", "CARINHOSO"],
  "cloudinary_public_id": "animais/luna"
}
```

### Solicitar adocao

```http
POST /api/v1/animais/11111111-1111-1111-1111-111111111111/solicitacoes
Authorization: Bearer <access_token>
```

Resposta `201`:

```json
{
  "id": "44444444-4444-4444-4444-444444444444",
  "animal_id": "11111111-1111-1111-1111-111111111111",
  "animal_nome": "Luna",
  "adotante_id": "22222222-2222-2222-2222-222222222222",
  "adotante_nome": "Maria Silva",
  "status": "PENDENTE",
  "criado_em": "2026-05-25T20:00:00",
  "atualizado_em": "2026-05-25T20:00:00"
}
```

### Conferir status da adocao

```http
GET /api/v1/adotantes/me/solicitacoes
Authorization: Bearer <access_token>
```

Resposta `200`:

```json
[
  {
    "id": "44444444-4444-4444-4444-444444444444",
    "animal_id": "11111111-1111-1111-1111-111111111111",
    "animal_nome": "Luna",
    "adotante_id": "22222222-2222-2222-2222-222222222222",
    "adotante_nome": "Maria Silva",
    "status": "APROVADA",
    "criado_em": "2026-05-25T20:00:00",
    "atualizado_em": "2026-05-26T10:00:00"
  }
]
```

## Regras Do Fluxo

- Um animal `DISPONIVEL` pode receber solicitacoes de varios adotantes.
- Um mesmo adotante nao pode manter duas solicitacoes `PENDENTE` para o mesmo animal.
- O animal permanece `DISPONIVEL` enquanto os pedidos aguardam decisao.
- Ao aprovar um pedido, o animal passa para `ADOTADO` e as outras solicitacoes pendentes
  do mesmo animal sao rejeitadas.
- O operador so pode listar e decidir solicitacoes dos animais pertencentes ao proprio lar.
- A rota antiga de adocao direta nao e exposta; adocao acontece pela aprovacao de uma solicitacao.

Status de solicitacao:

```text
PENDENTE
APROVADA
REJEITADA
CANCELADA
```

## Fluxo Do Operador

1. Responsavel cadastra o lar com `POST /api/v1/lares/cadastro`.
2. Responsavel faz login com `POST /api/v1/auth/login` e guarda o `access_token` do operador.
3. Cadastrar animal com `POST /api/v1/animais`, usando
   `Authorization: Bearer <access_token>`.
4. Listar candidatos com `GET /api/v1/lares/me/solicitacoes?status=PENDENTE`, usando
   `Authorization: Bearer <access_token>`.
5. Aprovar com `POST /api/v1/solicitacoes/{id}/aprovacao` ou rejeitar com
   `POST /api/v1/solicitacoes/{id}/rejeicao`, usando
   `Authorization: Bearer <access_token>`.

## Erros Relevantes

- `400`: dados invalidos, regra de dominio violada, email ou CPF duplicado.
- `401`: token ausente/invalido nas rotas protegidas.
- `403`: token sem o papel esperado, operador tentando acessar solicitacao de outro lar
  ou adotante tentando cancelar solicitacao de outro adotante.
- `404`: animal, lar ou solicitacao nao encontrada.
- `502`: falha de integracao Gemini ao gerar texto de divulgacao.
- `500`: erro interno inesperado.

## Pendencias Para Completar O Fluxo Do Adotante

- Migrar ou tratar senhas legadas que hoje estao em texto puro.
- Definir se ainda sera necessario um fluxo interno de `ADMIN` para moderacao e suporte.
