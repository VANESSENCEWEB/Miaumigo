# API Miaumigo - Solicitacao De Adocao

Contrato implementado para o fluxo MVP em que o adotante solicita a adocao e o operador do lar decide o pedido.

## Identidade Temporaria

A API ainda nao possui JWT. Os endpoints identificados abaixo exigem o header:

```http
X-Usuario-Id: <uuid-do-adotante-ou-operador>
```

Esse header e apenas uma simulacao para desenvolvimento. Ele nao comprova identidade e deve ser substituido por autenticacao real antes de exposicao publica. Header ausente retorna `401`; operador tentando decidir pedido de outro lar recebe `403`.

## Regras Do Fluxo

- Um animal `DISPONIVEL` pode receber solicitacoes de varios adotantes.
- Um mesmo adotante nao pode manter duas solicitacoes `PENDENTE` para o mesmo animal.
- O animal permanece `DISPONIVEL` enquanto os pedidos aguardam decisao.
- Ao aprovar um pedido, o animal passa a `ADOTADO` e as outras solicitacoes pendentes sao rejeitadas.
- O operador so pode listar e decidir solicitacoes dos animais pertencentes ao seu lar.
- A rota antiga de adocao direta nao e mais exposta; adocao acontece pela aprovacao de uma solicitacao.

Status de solicitacao:

```text
PENDENTE
APROVADA
REJEITADA
CANCELADA
```

## Endpoints

| Metodo | Rota | Identidade | Uso |
| --- | --- | --- | --- |
| `POST` | `/api/v1/adotantes` | Nao | Cadastra adotante |
| `GET` | `/api/v1/adotantes/me/animais-recomendados` | Adotante | Lista animais disponiveis ranqueados |
| `GET` | `/api/v1/adotantes/me/solicitacoes` | Adotante | Acompanha solicitacoes proprias |
| `POST` | `/api/v1/lares` | Nao | Provisiona lar no MVP |
| `POST` | `/api/v1/lares/{id}/operadores` | Nao | Provisiona operador do lar no MVP |
| `GET` | `/api/v1/lares/me/solicitacoes?status=PENDENTE` | Operador | Lista solicitacoes do lar |
| `POST` | `/api/v1/animais` | Operador | Cadastra animal no lar do operador |
| `GET` | `/api/v1/animais/{id}` | Nao | Exibe detalhe publico do animal |
| `POST` | `/api/v1/animais/{id}/solicitacoes` | Adotante | Cria solicitacao pendente |
| `POST` | `/api/v1/solicitacoes/{id}/cancelamento` | Adotante | Cancela solicitacao propria pendente |
| `POST` | `/api/v1/solicitacoes/{id}/aprovacao` | Operador | Aprova solicitacao pendente |
| `POST` | `/api/v1/solicitacoes/{id}/rejeicao` | Operador | Rejeita solicitacao pendente |

## Provisionamento Do Cenario

Criar lar:

```http
POST /api/v1/lares
Content-Type: application/json

{"nome": "Lar Amigo"}
```

Criar operador:

```http
POST /api/v1/lares/{lar_id}/operadores
Content-Type: application/json

{
  "nome": "Ana",
  "endereco": "Rua A, 10",
  "email": "ana@lar.com",
  "senha": "senha123",
  "cpf": "12345678901"
}
```

Criar animal como operador; `lar_id` nao deve ser enviado e nao e utilizado, pois o lar vem da identidade:

```http
POST /api/v1/animais
X-Usuario-Id: {operador_id}
Content-Type: application/json

{
  "nome": "Luna",
  "especie": "GATO",
  "porte": "PEQUENO",
  "idade": 2,
  "descricao": "Gata calma e carinhosa.",
  "tags": ["CALMO", "CARINHOSO"],
  "cloudinary_public_id": "animais/luna"
}
```

## Fluxo Do Adotante

1. Cadastrar-se com `POST /adotantes` e usar o `id` retornado como `X-Usuario-Id`.
2. Consultar `GET /adotantes/me/animais-recomendados`.
3. Abrir `GET /animais/{id}`; o detalhe inclui `descricao` e `status`.
4. Criar o pedido com `POST /animais/{id}/solicitacoes`, sem corpo.
5. Consultar `GET /adotantes/me/solicitacoes` ou cancelar com `POST /solicitacoes/{id}/cancelamento`.

Resposta de solicitacao:

```json
{
  "id": "44444444-4444-4444-4444-444444444444",
  "animal_id": "11111111-1111-1111-1111-111111111111",
  "animal_nome": "Luna",
  "adotante_id": "22222222-2222-2222-2222-222222222222",
  "adotante_nome": "Maria",
  "status": "PENDENTE",
  "criado_em": "2026-05-25T20:00:00",
  "atualizado_em": "2026-05-25T20:00:00"
}
```

## Fluxo Do Operador

1. Listar candidatos com `GET /lares/me/solicitacoes?status=PENDENTE`, usando o ID do operador no header.
2. Rejeitar individualmente com `POST /solicitacoes/{id}/rejeicao`; o animal continua disponivel.
3. Aprovar o candidato escolhido com `POST /solicitacoes/{id}/aprovacao`; o animal e marcado como adotado e os demais pedidos pendentes sao rejeitados.

## Pendencias Para Producao

- Implementar Spring Security, JWT e autorizacao real.
- Armazenar senhas com hash seguro; no estado atual elas ainda sao persistidas diretamente.
- Restringir os endpoints de provisionamento de lar e operador a administradores.
