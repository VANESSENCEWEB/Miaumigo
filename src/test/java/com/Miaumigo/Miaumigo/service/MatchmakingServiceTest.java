package com.Miaumigo.Miaumigo.service;

import com.Miaumigo.Miaumigo.domain.Adotante;
import com.Miaumigo.Miaumigo.domain.Animal;
import com.Miaumigo.Miaumigo.domain.AnimalStatus;
import com.Miaumigo.Miaumigo.domain.Especie;
import com.Miaumigo.Miaumigo.domain.Porte;
import com.Miaumigo.Miaumigo.domain.Tag;
import com.Miaumigo.Miaumigo.dto.AnimalRecomendadoResponse;
import com.Miaumigo.Miaumigo.exception.IdentidadeNaoAutenticadaException;
import com.Miaumigo.Miaumigo.repository.AdotanteRepository;
import com.Miaumigo.Miaumigo.repository.AnimalRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MatchmakingServiceTest {

	private final AnimalRepository animalRepository = mock(AnimalRepository.class);
	private final AdotanteRepository adotanteRepository = mock(AdotanteRepository.class);
	private final MatchmakingService matchmakingService = new MatchmakingService(animalRepository, adotanteRepository);

	@Test
	void deveOrdenarAnimaisPorCompatibilidade_quandoAdotantePossuiPreferencias() {
		UUID adotanteId = UUID.randomUUID();
		Adotante adotante = novoAdotante(List.of(Tag.CALMO, Tag.CARINHOSO, Tag.CONVIVE_COM_GATOS));
		Animal semMatch = novoAnimal("Thor", List.of(Tag.ENERGICO));
		Animal doisMatches = novoAnimal("Luna", List.of(Tag.CALMO, Tag.CONVIVE_COM_GATOS));
		Animal umMatch = novoAnimal("Mel", List.of(Tag.CARINHOSO));
		when(adotanteRepository.findById(adotanteId)).thenReturn(Optional.of(adotante));
		when(animalRepository.findByStatus(AnimalStatus.DISPONIVEL))
				.thenReturn(List.of(semMatch, doisMatches, umMatch));

		List<AnimalRecomendadoResponse> recomendacoes = matchmakingService.recomendarAnimais(adotanteId);

		assertEquals(List.of("Luna", "Mel", "Thor"), recomendacoes.stream().map(AnimalRecomendadoResponse::nome).toList());
		assertEquals(List.of(2, 1, 0), recomendacoes.stream().map(AnimalRecomendadoResponse::compatibilidade).toList());
		verify(animalRepository).findByStatus(AnimalStatus.DISPONIVEL);
	}

	@Test
	void devePreservarOrdemOriginal_quandoCompatibilidadesEmpatadas() {
		UUID adotanteId = UUID.randomUUID();
		when(adotanteRepository.findById(adotanteId)).thenReturn(Optional.of(novoAdotante(List.of(Tag.CALMO))));
		when(animalRepository.findByStatus(AnimalStatus.DISPONIVEL))
				.thenReturn(List.of(
						novoAnimal("Primeiro", List.of(Tag.CALMO)),
						novoAnimal("Segundo", List.of(Tag.CALMO))
				));

		List<AnimalRecomendadoResponse> recomendacoes = matchmakingService.recomendarAnimais(adotanteId);

		assertEquals(List.of("Primeiro", "Segundo"), recomendacoes.stream().map(AnimalRecomendadoResponse::nome).toList());
	}

	@Test
	void deveRetornarAnimaisComZeroCompatibilidade_quandoAdotanteSemPreferencias() {
		UUID adotanteId = UUID.randomUUID();
		when(adotanteRepository.findById(adotanteId)).thenReturn(Optional.of(novoAdotante(List.of())));
		when(animalRepository.findByStatus(AnimalStatus.DISPONIVEL))
				.thenReturn(List.of(novoAnimal("Luna", List.of(Tag.CALMO))));

		List<AnimalRecomendadoResponse> recomendacoes = matchmakingService.recomendarAnimais(adotanteId);

		assertEquals(1, recomendacoes.size());
		assertEquals(0, recomendacoes.getFirst().compatibilidade());
	}

	@Test
	void deveRetornarListaVazia_quandoNaoExistiremAnimaisDisponiveis() {
		UUID adotanteId = UUID.randomUUID();
		when(adotanteRepository.findById(adotanteId)).thenReturn(Optional.of(novoAdotante(List.of(Tag.CALMO))));
		when(animalRepository.findByStatus(AnimalStatus.DISPONIVEL)).thenReturn(List.of());

		List<AnimalRecomendadoResponse> recomendacoes = matchmakingService.recomendarAnimais(adotanteId);

		assertEquals(List.of(), recomendacoes);
	}

	@Test
	void deveLancarExcecao_quandoAdotanteNaoEncontrado() {
		UUID adotanteId = UUID.randomUUID();
		when(adotanteRepository.findById(adotanteId)).thenReturn(Optional.empty());

		IdentidadeNaoAutenticadaException exception = assertThrows(
				IdentidadeNaoAutenticadaException.class,
				() -> matchmakingService.recomendarAnimais(adotanteId)
		);

		assertEquals("Adotante não autenticado.", exception.getMessage());
	}

	private Adotante novoAdotante(List<Tag> preferencias) {
		return new Adotante("Maria Silva", "Rua das Flores", "maria@email.com", "senha", "12345678901", preferencias);
	}

	private Animal novoAnimal(String nome, List<Tag> tags) {
		return new Animal(nome, Especie.GATO, Porte.PEQUENO, 2, "Descrição", UUID.randomUUID(), tags, null);
	}
}
