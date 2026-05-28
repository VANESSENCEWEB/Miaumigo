package com.Miaumigo.Miaumigo.dto;

import java.util.UUID;

public record UsuarioAutenticadoResponse(
		UUID id,
		String nome,
		String email,
		String papel
) {
}
