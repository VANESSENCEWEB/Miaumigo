package com.Miaumigo.Miaumigo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MensagemSuporteRequest(
		@NotBlank(message = "Assunto é obrigatório")
		@Size(max = 120, message = "Assunto deve ter no máximo 120 caracteres")
		String assunto,

		@NotBlank(message = "Mensagem é obrigatória")
		@Size(min = 10, max = 1000, message = "Mensagem deve ter entre 10 e 1000 caracteres")
		String mensagem
) {
}
