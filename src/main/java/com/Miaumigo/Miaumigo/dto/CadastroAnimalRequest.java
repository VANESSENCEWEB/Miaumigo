package com.Miaumigo.Miaumigo.dto;

import com.Miaumigo.Miaumigo.domain.Especie;
import com.Miaumigo.Miaumigo.domain.Porte;
import com.Miaumigo.Miaumigo.domain.Tag;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.List;

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

		List<Tag> tags,


		//Verificar se esse realmente é o nome da chave dada pelo cloudnary
			@JsonProperty("cloudinary_public_id")
			String cloudinaryPublicId
) {
}
