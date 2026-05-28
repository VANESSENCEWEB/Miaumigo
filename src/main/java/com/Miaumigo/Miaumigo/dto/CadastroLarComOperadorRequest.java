package com.Miaumigo.Miaumigo.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CadastroLarComOperadorRequest(
		@NotBlank(message = "Nome do lar é obrigatório")
		String nome,

		@Valid
		@NotNull(message = "Operador responsável é obrigatório")
		CadastroOperadorRequest operador
) {
}
