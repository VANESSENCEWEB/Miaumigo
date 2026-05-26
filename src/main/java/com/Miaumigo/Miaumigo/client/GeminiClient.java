package com.Miaumigo.Miaumigo.client;

import com.Miaumigo.Miaumigo.dto.gemini.GeminiGenerateContentRequest;
import com.Miaumigo.Miaumigo.dto.gemini.GeminiGenerateContentResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "geminiClient", url = "${gemini.api.url:https://generativelanguage.googleapis.com}")
public interface GeminiClient {

	@PostMapping("/v1beta/models/{model}:generateContent")
	GeminiGenerateContentResponse gerarConteudo(
			@PathVariable String model,
			@RequestHeader("x-goog-api-key") String apiKey,
			@RequestBody GeminiGenerateContentRequest request
	);
}
