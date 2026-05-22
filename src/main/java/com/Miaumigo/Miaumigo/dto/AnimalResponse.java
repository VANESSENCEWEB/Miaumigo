package com.Miaumigo.Miaumigo.dto;

import com.Miaumigo.Miaumigo.domain.Especie;
import com.Miaumigo.Miaumigo.domain.Porte;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.UUID;

public record AnimalResponse(
		UUID id,
		String nome,
		Integer idade,
		Porte porte,
		Especie especie,
		List<String> tags,

		@JsonProperty("cloudinary_public_id")
		String cloudinaryPublicId
) {
}
