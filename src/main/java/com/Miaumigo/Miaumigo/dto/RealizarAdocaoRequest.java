package com.Miaumigo.Miaumigo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record RealizarAdocaoRequest(
		@JsonProperty("adotante_id")
		@NotNull(message = "Adotante é obrigatório")
		UUID adotanteId
) {
}
