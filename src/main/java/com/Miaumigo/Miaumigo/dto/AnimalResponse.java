package com.Miaumigo.Miaumigo.dto;

import com.Miaumigo.Miaumigo.domain.Animal;
import com.Miaumigo.Miaumigo.domain.AnimalStatus;
import com.Miaumigo.Miaumigo.domain.Especie;
import com.Miaumigo.Miaumigo.domain.Porte;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.UUID;

public record AnimalResponse(
		UUID id,
		String nome,
		Especie especie,
		Porte porte,
		Integer idade,
		String descricao,
		AnimalStatus status,
		@JsonProperty("lar_id")
		UUID larId,
		@JsonProperty("criado_em")
		LocalDateTime criadoEm,
		@JsonProperty("atualizado_em")
		LocalDateTime atualizadoEm
) {
	public static AnimalResponse from(Animal animal) {
		return new AnimalResponse(
				animal.getId(),
				animal.getNome(),
				animal.getEspecie(),
				animal.getPorte(),
				animal.getIdade(),
				animal.getDescricao(),
				animal.getStatus(),
				animal.getLarId(),
				animal.getCriadoEm(),
				animal.getAtualizadoEm()
		);
	}
}
