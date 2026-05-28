package com.Miaumigo.Miaumigo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

public record LoginResponse(
		@JsonProperty("access_token")
		String accessToken,

		@JsonProperty("token_type")
		String tokenType,

		@JsonProperty("expira_em")
		Instant expiraEm,

		UsuarioAutenticadoResponse usuario
) {
}
