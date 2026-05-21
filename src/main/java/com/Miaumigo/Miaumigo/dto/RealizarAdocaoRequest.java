package com.Miaumigo.Miaumigo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record RealizarAdocaoRequest(
		//record temporario para testes: REMOVER QUANDO ENTIDADE USUARIO EXISTIR
		@JsonProperty("adotado_por")
		@NotBlank(message = "Responsável pela adoção é obrigatório")
		String adotadoPor
) {
}
