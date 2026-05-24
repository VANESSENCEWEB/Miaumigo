package com.Miaumigo.Miaumigo.dto;

import com.Miaumigo.Miaumigo.domain.Tag;

import java.util.List;
import java.util.UUID;

public record AdotanteResponse(
		UUID id,
		String nome,
		String endereco,
		String email,
		String cpf,
		List<Tag> preferencias
) {
}
