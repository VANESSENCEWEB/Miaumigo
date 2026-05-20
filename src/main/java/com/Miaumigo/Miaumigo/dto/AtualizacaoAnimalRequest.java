package com.Miaumigo.Miaumigo.dto;

import com.Miaumigo.Miaumigo.domain.Especie;
import com.Miaumigo.Miaumigo.domain.Porte;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record AtualizacaoAnimalRequest(
		@NotBlank(message = "Nome do animal é obrigatório")
		String nome,

		@NotNull(message = "Espécie do animal é obrigatória")
		Especie especie,

		@NotNull(message = "Porte do animal é obrigatório")
		Porte porte,

		@PositiveOrZero(message = "Idade do animal não pode ser negativa")
		Integer idade,

		String descricao
) {
}
