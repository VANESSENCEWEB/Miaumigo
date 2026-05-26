package com.Miaumigo.Miaumigo.service;

import com.Miaumigo.Miaumigo.client.GeminiClient;
import com.Miaumigo.Miaumigo.domain.Adotante;
import com.Miaumigo.Miaumigo.domain.Animal;
import com.Miaumigo.Miaumigo.domain.Especie;
import com.Miaumigo.Miaumigo.domain.Porte;
import com.Miaumigo.Miaumigo.domain.Tag;
import com.Miaumigo.Miaumigo.dto.TextoDivulgacaoResponse;
import com.Miaumigo.Miaumigo.dto.gemini.GeminiGenerateContentRequest;
import com.Miaumigo.Miaumigo.dto.gemini.GeminiGenerateContentResponse;
import com.Miaumigo.Miaumigo.exception.IntegracaoGeminiException;
import com.Miaumigo.Miaumigo.exception.RecursoNaoEncontradoException;
import com.Miaumigo.Miaumigo.repository.AnimalRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TextoDivulgacaoServiceTest {

	private final AnimalRepository animalRepository = mock(AnimalRepository.class);
	private final GeminiClient geminiClient = mock(GeminiClient.class);
	private final TextoDivulgacaoService service = new TextoDivulgacaoService(
			animalRepository,
			geminiClient,
			"api-key-teste",
			"gemini-2.5-flash"
	);

	@Test
	void deveGerarTexto_quandoAnimalDisponivel() {
		UUID id = UUID.randomUUID();
		Animal animal = new Animal(
				"Luna", Especie.GATO, Porte.PEQUENO, 2, "Calma e carinhosa",
				UUID.randomUUID(), List.of(Tag.CALMO, Tag.CARINHOSO), null
		);
		when(animalRepository.findById(id)).thenReturn(Optional.of(animal));
		when(geminiClient.gerarConteudo(eq("gemini-2.5-flash"), eq("api-key-teste"), any()))
				.thenReturn(response("Conheça Luna! Adote com amor. #Adote"));

		TextoDivulgacaoResponse response = service.gerar(id);

		assertEquals("Conheça Luna! Adote com amor. #Adote", response.texto());
		ArgumentCaptor<GeminiGenerateContentRequest> captor =
				ArgumentCaptor.forClass(GeminiGenerateContentRequest.class);
		verify(geminiClient).gerarConteudo(eq("gemini-2.5-flash"), eq("api-key-teste"), captor.capture());
		String prompt = captor.getValue().contents().getFirst().parts().getFirst().text();
		assertTrue(prompt.contains("Nome: Luna"));
		assertTrue(prompt.contains("Idade: 2 ano(s)"));
		assertTrue(prompt.contains("Descrição: Calma e carinhosa"));
		assertTrue(prompt.contains("Características: CALMO, CARINHOSO"));
		assertTrue(prompt.contains("Não invente informações"));
	}

	@Test
	void deveOmitirDadosOpcionaisAusentesDoPrompt() {
		UUID id = UUID.randomUUID();
		Animal animal = new Animal("Bob", Especie.CACHORRO, Porte.MEDIO, null, null, UUID.randomUUID());
		when(animalRepository.findById(id)).thenReturn(Optional.of(animal));
		when(geminiClient.gerarConteudo(any(), any(), any())).thenReturn(response("Texto"));

		service.gerar(id);

		ArgumentCaptor<GeminiGenerateContentRequest> captor =
				ArgumentCaptor.forClass(GeminiGenerateContentRequest.class);
		verify(geminiClient).gerarConteudo(any(), any(), captor.capture());
		String prompt = captor.getValue().contents().getFirst().parts().getFirst().text();
		assertFalse(prompt.contains("Idade:"));
		assertFalse(prompt.contains("Descrição:"));
		assertFalse(prompt.contains("Características:"));
	}

	@Test
	void deveRejeitarAnimalNaoDisponivel_semConsultarGemini() {
		UUID id = UUID.randomUUID();
		Animal animal = new Animal("Luna", Especie.GATO, Porte.PEQUENO, 2, null, UUID.randomUUID());
		animal.realizarAdocao(new Adotante(
				"Maria", "Rua Um", "maria@email.com", "senha", "12345678901", List.of()
		));
		when(animalRepository.findById(id)).thenReturn(Optional.of(animal));

		IllegalStateException exception = assertThrows(IllegalStateException.class, () -> service.gerar(id));

		assertEquals("Somente animais disponíveis podem ter texto de divulgação gerado.", exception.getMessage());
		verify(geminiClient, never()).gerarConteudo(any(), any(), any());
	}

	@Test
	void deveLancarNotFound_quandoAnimalNaoExistir() {
		UUID id = UUID.randomUUID();
		when(animalRepository.findById(id)).thenReturn(Optional.empty());

		assertThrows(RecursoNaoEncontradoException.class, () -> service.gerar(id));
		verify(geminiClient, never()).gerarConteudo(any(), any(), any());
	}

	@Test
	void deveConverterFalhaDoGeminiEmErroDeIntegracao() {
		UUID id = UUID.randomUUID();
		when(animalRepository.findById(id)).thenReturn(Optional.of(
				new Animal("Luna", Especie.GATO, Porte.PEQUENO, 2, null, UUID.randomUUID())
		));
		when(geminiClient.gerarConteudo(any(), any(), any())).thenThrow(new RuntimeException("timeout"));

		IntegracaoGeminiException exception = assertThrows(IntegracaoGeminiException.class, () -> service.gerar(id));

		assertEquals("Não foi possível gerar o texto de divulgação.", exception.getMessage());
	}

	@Test
	void deveRejeitarRespostaSemTexto() {
		UUID id = UUID.randomUUID();
		when(animalRepository.findById(id)).thenReturn(Optional.of(
				new Animal("Luna", Especie.GATO, Porte.PEQUENO, 2, null, UUID.randomUUID())
		));
		when(geminiClient.gerarConteudo(any(), any(), any()))
				.thenReturn(new GeminiGenerateContentResponse(List.of()));

		assertThrows(IntegracaoGeminiException.class, () -> service.gerar(id));
	}

	private GeminiGenerateContentResponse response(String texto) {
		return new GeminiGenerateContentResponse(List.of(
				new GeminiGenerateContentResponse.Candidate(
						new GeminiGenerateContentResponse.Content(List.of(
								new GeminiGenerateContentResponse.Part(texto)
						))
				)
		));
	}
}
