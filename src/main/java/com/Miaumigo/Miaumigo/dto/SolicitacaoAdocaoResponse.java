package com.Miaumigo.Miaumigo.dto;

import com.Miaumigo.Miaumigo.domain.SolicitacaoStatus;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.UUID;

public record SolicitacaoAdocaoResponse(
		UUID id,

		@JsonProperty("animal_id")
		UUID animalId,

		@JsonProperty("animal_nome")
		String animalNome,

		@JsonProperty("adotante_id")
		UUID adotanteId,

		@JsonProperty("adotante_nome")
		String adotanteNome,

		SolicitacaoStatus status,

		@JsonProperty("criado_em")
		LocalDateTime criadoEm,

		@JsonProperty("atualizado_em")
		LocalDateTime atualizadoEm
) {
}
