package com.Miaumigo.Miaumigo.dto;

import com.Miaumigo.Miaumigo.domain.MensagemSuporteStatus;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.UUID;

public record MensagemSuporteResponse(
		UUID id,
		String assunto,
		String mensagem,
		MensagemSuporteStatus status,

		@JsonProperty("criado_em")
		LocalDateTime criadoEm
) {
}
