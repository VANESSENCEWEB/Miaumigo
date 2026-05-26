package com.Miaumigo.Miaumigo.dto;

import jakarta.validation.constraints.NotBlank;

public record CadastroOperadorRequest(
		@NotBlank(message = "Nome do operador é obrigatório")
		String nome,

		@NotBlank(message = "Endereço do operador é obrigatório")
		String endereco,

		@NotBlank(message = "Email do operador é obrigatório")
		String email,

		@NotBlank(message = "Senha do operador é obrigatória")
		String senha,

		@NotBlank(message = "CPF do operador é obrigatório")
		String cpf
) {
}
