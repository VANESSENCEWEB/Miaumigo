package com.Miaumigo.Miaumigo.dto;

import com.Miaumigo.Miaumigo.domain.Role;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record OperadorResponse(
		UUID id,
		String nome,
		String email,
		Role role,

		@JsonProperty("lar_id")
		UUID larId
) {
}
