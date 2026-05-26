package com.Miaumigo.Miaumigo.dto.gemini;

import java.util.List;
import java.util.Optional;

public record GeminiGenerateContentResponse(List<Candidate> candidates) {

	public Optional<String> textoGerado() {
		if (candidates == null || candidates.isEmpty()) {
			return Optional.empty();
		}
		Content content = candidates.getFirst().content();
		if (content == null || content.parts() == null || content.parts().isEmpty()) {
			return Optional.empty();
		}
		return Optional.ofNullable(content.parts().getFirst().text())
				.map(String::trim)
				.filter(texto -> !texto.isEmpty());
	}

	public record Candidate(Content content) {
	}

	public record Content(List<Part> parts) {
	}

	public record Part(String text) {
	}
}
