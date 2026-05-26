package com.Miaumigo.Miaumigo.dto;

import jakarta.validation.constraints.NotBlank;

public record CadastroLarRequest(
		@NotBlank(message = "Nome do lar é obrigatório")
		String nome
) {
}
