package com.Miaumigo.Miaumigo.dto.gemini;

import java.util.List;

public record GeminiGenerateContentRequest(List<Content> contents) {

	public static GeminiGenerateContentRequest comPrompt(String prompt) {
		return new GeminiGenerateContentRequest(List.of(new Content(List.of(new Part(prompt)))));
	}

	public record Content(List<Part> parts) {
	}

	public record Part(String text) {
	}
}
