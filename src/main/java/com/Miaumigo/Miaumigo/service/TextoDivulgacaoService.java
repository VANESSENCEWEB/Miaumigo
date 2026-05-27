package com.Miaumigo.Miaumigo.service;

import com.Miaumigo.Miaumigo.client.GeminiClient;
import com.Miaumigo.Miaumigo.domain.Animal;
import com.Miaumigo.Miaumigo.domain.AnimalStatus;
import com.Miaumigo.Miaumigo.dto.TextoDivulgacaoResponse;
import com.Miaumigo.Miaumigo.dto.gemini.GeminiGenerateContentRequest;
import com.Miaumigo.Miaumigo.dto.gemini.GeminiGenerateContentResponse;
import com.Miaumigo.Miaumigo.exception.IntegracaoGeminiException;
import com.Miaumigo.Miaumigo.exception.RecursoNaoEncontradoException;
import com.Miaumigo.Miaumigo.repository.AnimalRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TextoDivulgacaoService {

	private static final String MENSAGEM_ERRO_GEMINI = "Não foi possível gerar o texto de divulgação.";

	private final AnimalRepository animalRepository;
	private final GeminiClient geminiClient;
	private final String apiKey;
	private final String model;

	public TextoDivulgacaoService(
			AnimalRepository animalRepository,
			GeminiClient geminiClient,
			@Value("${gemini.api-key:}") String apiKey,
			@Value("${gemini.model:gemini-2.5-flash}") String model
	) {
		this.animalRepository = animalRepository;
		this.geminiClient = geminiClient;
		this.apiKey = apiKey;
		this.model = model;
	}

	@Transactional(readOnly = true)
	public TextoDivulgacaoResponse gerar(UUID animalId) {
		Animal animal = animalRepository.findById(animalId)
				.orElseThrow(() -> new RecursoNaoEncontradoException("Animal não encontrado."));

		if (animal.getStatus() != AnimalStatus.DISPONIVEL) {
			throw new IllegalStateException("Somente animais disponíveis podem ter texto de divulgação gerado.");
		}
		if (apiKey == null || apiKey.isBlank()) {
			throw new IntegracaoGeminiException(MENSAGEM_ERRO_GEMINI);
		}

		GeminiGenerateContentResponse response;
		try {
			response = geminiClient.gerarConteudo(
					model,
					apiKey,
					GeminiGenerateContentRequest.comPrompt(criarPrompt(animal))
			);
		} catch (RuntimeException exception) {
			throw new IntegracaoGeminiException(MENSAGEM_ERRO_GEMINI, exception);
		}

		String texto = response == null
				? null
				: response.textoGerado().orElse(null);
		if (texto == null) {
			throw new IntegracaoGeminiException(MENSAGEM_ERRO_GEMINI);
		}

		return new TextoDivulgacaoResponse(texto);
	}

	private String criarPrompt(Animal animal) {
		StringBuilder prompt = new StringBuilder("""
				Crie uma legenda curta em português brasileiro para divulgar um animal disponível para adoção.
				O texto deve ser acolhedor, apropriado para Instagram ou WhatsApp, incluir uma chamada para adoção e até 4 hashtags.
				Não invente informações que não estejam nos dados fornecidos, como localização, contato, vacinação ou castração.

				Dados do animal:
				""");
		prompt.append("Nome: ").append(animal.getNome()).append('\n');
		prompt.append("Espécie: ").append(animal.getEspecie()).append('\n');
		prompt.append("Porte: ").append(animal.getPorte()).append('\n');
		if (animal.getIdade() != null) {
			prompt.append("Idade: ").append(animal.getIdade()).append(" ano(s)\n");
		}
		if (animal.getDescricao() != null && !animal.getDescricao().isBlank()) {
			prompt.append("Descrição: ").append(animal.getDescricao().trim()).append('\n');
		}
		if (!animal.getTags().isEmpty()) {
			prompt.append("Características: ")
					.append(animal.getTags().stream().map(Enum::name).collect(Collectors.joining(", ")))
					.append('\n');
		}
		return prompt.toString();
	}
}
