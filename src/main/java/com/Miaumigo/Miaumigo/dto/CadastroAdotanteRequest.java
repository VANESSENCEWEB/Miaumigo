package com.Miaumigo.Miaumigo.dto;

import com.Miaumigo.Miaumigo.domain.Tag;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record CadastroAdotanteRequest(
		@NotBlank(message = "Nome do adotante é obrigatório")
		String nome,

		@NotBlank(message = "Endereço do adotante é obrigatório")
		String endereco,

		@NotBlank(message = "Email do adotante é obrigatório")
		String email,

		@NotBlank(message = "Senha do adotante é obrigatória")
		String senha,

		@NotBlank(message = "CPF do adotante é obrigatório")
		String cpf,

		List<Tag> preferencias
) {
}
