# Unificar Perfil Progressivo, Matchmaking e Adoção

## Summary

Adotar a opção C: cadastro inicial continua leve, mas o usuário completa um **perfil progressivo de adoção** uma única vez. Esse perfil passa a ser persistido no backend, reaproveitado pelo frontend e usado de fato no algoritmo de matchmaking.

A regra principal: **nenhum formulário deve pedir dado que não será persistido ou usado**. Dados de compatibilidade alimentam o match; dados de contato/adoção só aparecem se forem realmente necessários para a solicitação.

## Key Changes

- Criar um perfil persistido do adotante com campos nullable para usuários existentes:
  - `especies_preferidas`: lista de `Especie`
  - `preferencias`: manter lista atual de `Tag`
  - `tipo_moradia`: `APARTAMENTO`, `CASA`, `CASA_COM_QUINTAL`, `SITIO_CHACARA`
  - `espaco_disponivel`: `PEQUENO`, `MEDIO`, `GRANDE`
  - `tempo_disponivel`: `ATE_30_MIN`, `UMA_HORA`, `DUAS_HORAS_OU_MAIS`
  - `experiencia_animais`: `PRIMEIRA_ADOCAO`, `JA_TIVE_PETS`, `TENHO_PETS_HOJE`
  - `possui_criancas`, `possui_caes`, `possui_gatos`
  - `telefone` e `cidade` apenas como dados de contato para adoção, não como critérios centrais de match.

- Adicionar endpoints autenticados:
  - `GET /api/v1/adotantes/me`: retorna dados do adotante, perfil de compatibilidade e `perfil_completo`.
  - `PATCH /api/v1/adotantes/me/perfil`: atualiza o perfil progressivo.
  - Manter `POST /api/v1/adotantes` compatível com o payload atual, aceitando os novos campos como opcionais.

- Alterar matchmaking para pontuar por preferências e capacidade do adotante:
  - Preferência positiva: espécie preferida, tags desejadas, porte compatível.
  - Necessidades do animal que o adotante precisa suprir:
    - `PRECISA_DE_ESPACO` exige espaço `GRANDE` ou moradia com quintal/sítio.
    - `ADAPTADO_A_APARTAMENTO` favorece apartamento/espaço pequeno.
    - `ENERGICO` favorece `DUAS_HORAS_OU_MAIS`.
    - animal de porte `GRANDE` penaliza espaço `PEQUENO`.
    - `CONVIVE_COM_CRIANCAS`, `CONVIVE_COM_CAES`, `CONVIVE_COM_GATOS` favorecem lares que têm esses contextos.
    - perfis mais exigentes, como `PROTETOR` ou `ENERGICO`, penalizam `PRIMEIRA_ADOCAO`.
  - Retornar `compatibilidade` como porcentagem visível ao usuário e usada para ordenação. Não exibir justificativas do cálculo; para o usuário final, mostrar somente a porcentagem e as tags/comportamentos do animal.

- Ajustar frontend:
  - Após login/cadastro, carregar `GET /api/v1/adotantes/me` e guardar o perfil na sessão/app state.
  - Tela de match:
    - Se `perfil_completo=false`, mostrar formulário “Complete seu perfil”.
    - Se completo, chamar recomendações diretamente.
    - Remover formulário de match que hoje não influencia o backend.
  - Tela de adoção:
    - Não pedir moradia/rotina de novo.
    - Mostrar resumo dos dados do perfil.
    - Se faltarem telefone/cidade, pedir só esses dados e persistir via perfil antes de solicitar adoção.
  - Cadastro:
    - Manter preferências básicas, mas não obrigar todos os dados avançados.

## Risks And Mitigations

- **Risco: cadastro ficar pesado.**
  Mitigação: manter cadastro leve e completar perfil só no primeiro match/adoção.

- **Risco: usuários antigos sem perfil completo quebrarem o match.**
  Mitigação: campos novos nullable; frontend direciona para completar perfil; backend retorna lista genérica ordenada por tags quando perfil estiver incompleto, se necessário.

- **Risco: algoritmo ficar difícil de auditar internamente.**
  Mitigação: manter testes claros para as regras de pontuação e, se necessário, logs internos sem expor justificativas ao usuário final.

- **Risco: usar tags para necessidades e personalidade misturadas.**
  Mitigação inicial: manter `Tag` para v1, documentando quais tags são necessidades. Evolução futura pode separar `PersonalidadeAnimal` de `NecessidadeAnimal`.

- **Risco: migrations em produção.**
  Mitigação: adicionar apenas colunas/tabelas nullable; não remover campos existentes; manter payloads antigos aceitos.

- **Risco: dados de adoção ainda não aparecerem para operadores.**
  Mitigação: em uma segunda etapa, enriquecer respostas de solicitações do lar com resumo do adotante/perfil. Para v1, priorizar eliminar repetição e fazer o match usar os dados.

## Test Plan

- Backend service tests:
  - `deveCalcularCompatibilidadeMaior_quandoAnimalAtendePreferenciasENecessidadesDoAdotante`
  - `devePenalizarAnimalEnergetico_quandoAdotanteTemPoucoTempo`
  - `devePenalizarAnimalQuePrecisaDeEspaco_quandoAdotanteTemEspacoPequeno`
  - `deveIndicarPerfilIncompleto_quandoCamposObrigatoriosDoMatchFaltam`
  - `deveAtualizarPerfilProgressivo_quandoPayloadValido`

- Backend controller tests:
  - `GET /api/v1/adotantes/me` autenticado retorna perfil e `perfil_completo`.
  - `PATCH /api/v1/adotantes/me/perfil` persiste novos campos.
  - endpoints protegidos continuam retornando `401` sem token.

- Frontend scenarios:
  - usuário novo cadastra, entra no match, completa perfil uma vez e vê recomendações.
  - usuário com perfil completo entra no match sem repetir formulário.
  - adoção reaproveita perfil e só pede dados faltantes de contato.
  - campos preenchidos no formulário de perfil aparecem persistidos após refresh/login.

## Assumptions

- A primeira versão não cria uma tabela separada de necessidades do animal; usa `Tag` existente com regras explícitas.
- `telefone` e `cidade` serão persistidos no perfil do adotante, mas não pesarão no matchmaking.
- Solicitação de adoção continuará sendo criada com `animal_id + adotante autenticado`; os dados do perfil serão reaproveitados, não reenviados como formulário duplicado.
