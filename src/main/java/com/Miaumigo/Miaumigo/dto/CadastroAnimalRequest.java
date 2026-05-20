package com.Miaumigo.Miaumigo.dto;

import com.Miaumigo.Miaumigo.domain.Especie;
import com.Miaumigo.Miaumigo.domain.Porte;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.List;
import java.util.UUID;

public record CadastroAnimalRequest(
		@NotBlank(message = "Nome do animal é obrigatório")
		String nome,

		@NotNull(message = "Espécie do animal é obrigatória")
		Especie especie,

		@NotNull(message = "Porte do animal é obrigatório")
		Porte porte,

		@PositiveOrZero(message = "Idade do animal não pode ser negativa")
		Integer idade,

		String descricao,

		List<String> tags,


		//Verificar se esse realmente é o nome da chave dada pelo cloudnary
		@JsonProperty("cloudinary_public_id")
		String cloudinaryPublicId,

		@JsonProperty("lar_id")
		@NotNull(message = "Lar do animal é obrigatório")
		UUID larId
) {
}
