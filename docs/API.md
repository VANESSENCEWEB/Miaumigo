# API Miaumigo - Contrato Atual Para Frontend

Documentacao da API implementada no backend no estado atual do projeto. Este contrato cobre cadastro de adotantes, cadastro e consulta de animais, recomendacoes por compatibilidade e registro de adocao.

## Informacoes Gerais

**Base URL local**

```text
http://localhost:8080/api/v1
```

**Formato**

- Requests com corpo usam `Content-Type: application/json`.
- Respostas sao JSON, exceto o cadastro de animal, que atualmente retorna corpo vazio.
- Identificadores sao UUIDs em formato string.

**Autenticacao**

Nenhuma autenticacao ou autorizacao esta aplicada nos endpoints implementados neste momento.

## Valores Enumerados

### Especie

```text
CACHORRO
GATO
OUTRO
```

### Porte

```text
PEQUENO
MEDIO
GRANDE
```

### Tags e Preferencias

Os mesmos valores sao aceitos no campo `tags` do animal e no campo `preferencias` do adotante:

```text
DOCIL
BRINCALHAO
CALMO
INDEPENDENTE
CARINHOSO
SOCIAL
PROTETOR
ENERGICO
ADAPTADO_A_APARTAMENTO
PRECISA_DE_ESPACO
CONVIVE_COM_CRIANCAS
CONVIVE_COM_CAES
CONVIVE_COM_GATOS
```

`CASTRADO` nao e um valor valido de tag.

## Endpoints Disponiveis

| Metodo | Rota | Uso |
| --- | --- | --- |
| `POST` | `/adotantes` | Cadastra um adotante e suas preferencias |
| `GET` | `/adotantes/{id}/animais-recomendados` | Lista animais disponiveis ranqueados para o adotante |
| `POST` | `/animais` | Cadastra um animal |
| `GET` | `/animais/{id}` | Busca os dados publicos de um animal |
| `POST` | `/animais/{id}/adocao` | Registra a adocao de um animal por um adotante |

## Adotantes

### Cadastrar Adotante

```http
POST /api/v1/adotantes
```

**Request**

| Campo | Tipo | Obrigatorio | Observacao |
| --- | --- | --- | --- |
| `nome` | `string` | Sim | Nao pode ser vazio |
| `endereco` | `string` | Sim | Nao pode ser vazio |
| `email` | `string` | Sim | Unico; armazenado em minusculas |
| `senha` | `string` | Sim | Nao pode ser vazia |
| `cpf` | `string` | Sim | Unico; deve resultar em 11 digitos apos remover pontuacao |
| `preferencias` | `string[]` | Nao | Lista de tags usadas no matchmaking |

```json
{
  "nome": "Maria Match",
  "endereco": "Rua das Flores, 123",
  "email": "maria.match@email.com",
  "senha": "senha123",
  "cpf": "12345678901",
  "preferencias": ["CALMO", "CARINHOSO", "CONVIVE_COM_GATOS"]
}
```

**Resposta `201 Created`**

```json
{
  "id": "22222222-2222-2222-2222-222222222222",
  "nome": "Maria Match",
  "endereco": "Rua das Flores, 123",
  "email": "maria.match@email.com",
  "cpf": "12345678901",
  "preferencias": ["CALMO", "CARINHOSO", "CONVIVE_COM_GATOS"]
}
```

Observacao para o frontend: a senha nunca e devolvida na resposta.

### Listar Animais Recomendados

```http
GET /api/v1/adotantes/{id}/animais-recomendados
```

Retorna apenas animais com status disponivel, ordenados do maior para o menor valor de `compatibilidade`.

**Path parameter**

| Parametro | Tipo | Descricao |
| --- | --- | --- |
| `id` | `UUID` | ID do adotante cadastrado |

**Calculo atual**

Cada preferencia do adotante encontrada nas tags do animal soma `1` a `compatibilidade`.

```text
preferencias = [CALMO, CARINHOSO, CONVIVE_COM_GATOS]
tags animal = [CALMO, CARINHOSO, CONVIVE_COM_GATOS]
compatibilidade = 3
```

Animais com compatibilidade `0` tambem sao retornados, ao final da lista. Em empate, a API preserva a ordem original retornada pelo banco.

**Resposta `200 OK`**

```json
[
  {
    "id": "96050da9-ff63-41b2-abaf-19241f52e3f8",
    "nome": "Luna",
    "idade": 2,
    "porte": "PEQUENO",
    "especie": "GATO",
    "tags": ["CALMO", "CARINHOSO", "CONVIVE_COM_GATOS"],
    "cloudinary_public_id": "animais/luna",
    "compatibilidade": 3
  },
  {
    "id": "875bddc7-6f9d-4a7a-8de5-38e218b1f753",
    "nome": "Bob",
    "idade": 4,
    "porte": "MEDIO",
    "especie": "CACHORRO",
    "tags": ["CALMO", "ADAPTADO_A_APARTAMENTO"],
    "cloudinary_public_id": "animais/bob",
    "compatibilidade": 1
  }
]
```

**Resposta `200 OK` sem animais disponiveis**

```json
[]
```

O campo `id` de cada item deve ser usado para abrir detalhes ou iniciar a adocao do animal selecionado.

## Animais

### Cadastrar Animal

```http
POST /api/v1/animais
```

**Request**

| Campo | Tipo | Obrigatorio | Observacao |
| --- | --- | --- | --- |
| `nome` | `string` | Sim | Nao pode ser vazio |
| `especie` | `string` | Sim | Valor de `Especie` |
| `porte` | `string` | Sim | Valor de `Porte` |
| `idade` | `integer` | Nao | Quando informado, deve ser zero ou maior |
| `descricao` | `string` | Nao | Nao e retornada no DTO publico atual |
| `tags` | `string[]` | Nao | Tags usadas no matchmaking |
| `cloudinary_public_id` | `string` | Nao | Identificador da imagem |
| `lar_id` | `UUID` | Sim | Identificador do lar responsavel |

```json
{
  "nome": "Luna",
  "especie": "GATO",
  "porte": "PEQUENO",
  "idade": 2,
  "descricao": "Gata calma e carinhosa.",
  "tags": ["CALMO", "CARINHOSO", "CONVIVE_COM_GATOS"],
  "cloudinary_public_id": "animais/luna",
  "lar_id": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Resposta `201 Created`**

Corpo vazio.

Nota de integracao: este endpoint ainda nao retorna o `id` criado. O ID do animal fica disponivel na resposta de recomendacoes e no endpoint de detalhe quando ja conhecido.

### Buscar Animal Por ID

```http
GET /api/v1/animais/{id}
```

**Resposta `200 OK`**

```json
{
  "id": "96050da9-ff63-41b2-abaf-19241f52e3f8",
  "nome": "Luna",
  "idade": 2,
  "porte": "PEQUENO",
  "especie": "GATO",
  "tags": ["CALMO", "CARINHOSO", "CONVIVE_COM_GATOS"],
  "cloudinary_public_id": "animais/luna"
}
```

Nota de integracao: `descricao`, `status` e `lar_id` nao fazem parte desta resposta no backend atual.

### Registrar Adocao

```http
POST /api/v1/animais/{id}/adocao
```

**Path parameter**

| Parametro | Tipo | Descricao |
| --- | --- | --- |
| `id` | `UUID` | ID do animal selecionado |

**Request**

```json
{
  "adotante_id": "22222222-2222-2222-2222-222222222222"
}
```

**Resposta `200 OK`**

```json
{
  "mensagem": "Adoção realizada com sucesso."
}
```

O animal adotado deixa de aparecer nas proximas respostas de recomendacoes, pois o ranking lista somente animais disponiveis.

## Erros

O formato de erro e:

```json
{
  "mensagem": "Mensagem legivel para exibicao ou tratamento",
  "erros": []
}
```

### Validacao de request - `400 Bad Request`

```json
{
  "mensagem": "Dados inválidos",
  "erros": [
    "nome: Nome do animal é obrigatório"
  ]
}
```

### Email ou CPF duplicado - `400 Bad Request`

```json
{
  "mensagem": "Email já cadastrado.",
  "erros": []
}
```

ou:

```json
{
  "mensagem": "CPF já cadastrado.",
  "erros": []
}
```

### Regra de dominio - `400 Bad Request`

Exemplo ao tentar adotar um animal indisponivel:

```json
{
  "mensagem": "Apenas animais disponíveis podem ser adotados.",
  "erros": []
}
```

### Recurso nao encontrado - `404 Not Found`

```json
{
  "mensagem": "Animal não encontrado.",
  "erros": []
}
```

ou:

```json
{
  "mensagem": "Adotante não encontrado.",
  "erros": []
}
```

### Erro inesperado - `500 Internal Server Error`

```json
{
  "mensagem": "Erro interno no servidor",
  "erros": []
}
```

## Fluxo Recomendado No Frontend

1. Cadastrar o adotante com suas `preferencias` e armazenar o `id` retornado.
2. Carregar o feed personalizado com `GET /adotantes/{id}/animais-recomendados`.
3. Usar o `id` presente no card recomendado para chamar `GET /animais/{id}` ao abrir o detalhe.
4. Ao confirmar a adocao, enviar `POST /animais/{id}/adocao` com o `adotante_id`.
5. Recarregar as recomendacoes apos a adocao; o animal adotado nao devera mais aparecer.

## Observacoes Do Contrato Atual

- Nao existe endpoint geral de listagem de animais; a listagem disponivel ao frontend hoje e a de recomendacoes por adotante.
- Nao existe endpoint de consulta individual de adotante apos o cadastro.
- Nao existe endpoint de atualizacao de preferencias.
- Nao existe autenticacao JWT aplicada ainda.
- Nao existe configuracao CORS implementada; se o frontend rodar em outra origem, sera necessario usar proxy de desenvolvimento ou adicionar CORS no backend.
- Nao existe endpoint de cadastro/consulta de lar nesta etapa; o cadastro de animal exige um `lar_id` previamente conhecido.
- O `cloudinary_public_id` e retornado como identificador; a composicao da URL final da imagem ainda deve ser alinhada com a configuracao do frontend/Cloudinary.
