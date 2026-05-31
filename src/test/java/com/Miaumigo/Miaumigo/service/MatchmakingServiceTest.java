package com.Miaumigo.Miaumigo.service;

import com.Miaumigo.Miaumigo.domain.Adotante;
import com.Miaumigo.Miaumigo.domain.Animal;
import com.Miaumigo.Miaumigo.domain.AnimalStatus;
import com.Miaumigo.Miaumigo.domain.Especie;
import com.Miaumigo.Miaumigo.domain.ExperienciaAnimais;
import com.Miaumigo.Miaumigo.domain.Porte;
import com.Miaumigo.Miaumigo.domain.Tag;
import com.Miaumigo.Miaumigo.domain.TempoDisponivel;
import com.Miaumigo.Miaumigo.domain.TipoMoradia;
import com.Miaumigo.Miaumigo.dto.AnimalRecomendadoResponse;
import com.Miaumigo.Miaumigo.exception.IdentidadeNaoAutenticadaException;
import com.Miaumigo.Miaumigo.repository.AdotanteRepository;
import com.Miaumigo.Miaumigo.repository.AnimalRepository;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

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
		assertEquals(List.of(63, 54, 36), recomendacoes.stream().map(AnimalRecomendadoResponse::compatibilidade).toList());
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
	void deveRemoverAnimaisDuplicados_quandoRepositorioRetornarMesmoIdMaisDeUmaVez() {
		UUID adotanteId = UUID.randomUUID();
		Animal luna = novoAnimal("Luna", List.of(Tag.CALMO));
		ReflectionTestUtils.setField(luna, "id", UUID.fromString("11111111-1111-1111-1111-111111111111"));
		when(adotanteRepository.findById(adotanteId)).thenReturn(Optional.of(novoAdotante(List.of(Tag.CALMO))));
		when(animalRepository.findByStatus(AnimalStatus.DISPONIVEL))
				.thenReturn(List.of(luna, luna));

		List<AnimalRecomendadoResponse> recomendacoes = matchmakingService.recomendarAnimais(adotanteId);

		assertEquals(1, recomendacoes.size());
		assertEquals("Luna", recomendacoes.getFirst().nome());
	}

	@Test
	void deveRetornarAnimaisComCompatibilidadeGenerica_quandoAdotanteSemPerfilCompleto() {
		UUID adotanteId = UUID.randomUUID();
		when(adotanteRepository.findById(adotanteId)).thenReturn(Optional.of(novoAdotante(List.of())));
		when(animalRepository.findByStatus(AnimalStatus.DISPONIVEL))
				.thenReturn(List.of(novoAnimal("Luna", List.of(Tag.CALMO))));

		List<AnimalRecomendadoResponse> recomendacoes = matchmakingService.recomendarAnimais(adotanteId);

		assertEquals(1, recomendacoes.size());
		assertEquals(58, recomendacoes.getFirst().compatibilidade());
	}

	@Test
	void deveCalcularCompatibilidadeMaior_quandoAnimalAtendePreferenciasENecessidadesDoAdotante() {
		UUID adotanteId = UUID.randomUUID();
		Adotante adotante = novoAdotante(List.of(Tag.CALMO, Tag.ADAPTADO_A_APARTAMENTO));
		adotante.atualizarPerfil(
				List.of(Especie.GATO),
				List.of(Tag.CALMO, Tag.ADAPTADO_A_APARTAMENTO),
				TipoMoradia.APARTAMENTO,
				Porte.PEQUENO,
				TempoDisponivel.UMA_HORA,
				ExperienciaAnimais.JA_TIVE_PETS,
				false,
				false,
				true,
				null,
				null
		);
		when(adotanteRepository.findById(adotanteId)).thenReturn(Optional.of(adotante));
		when(animalRepository.findByStatus(AnimalStatus.DISPONIVEL))
				.thenReturn(List.of(
						novoAnimal("Luna", List.of(Tag.CALMO, Tag.ADAPTADO_A_APARTAMENTO)),
						new Animal("Rex", Especie.CACHORRO, Porte.GRANDE, 4, "Descrição", UUID.randomUUID(), List.of(Tag.ENERGICO, Tag.PRECISA_DE_ESPACO), null)
				));

		List<AnimalRecomendadoResponse> recomendacoes = matchmakingService.recomendarAnimais(adotanteId);

		assertEquals("Luna", recomendacoes.getFirst().nome());
		assertEquals(98, recomendacoes.getFirst().compatibilidade());
	}

	@Test
	void devePenalizarAnimalEnergetico_quandoAdotanteTemPoucoTempo() {
		UUID adotanteId = UUID.randomUUID();
		Adotante adotante = novoAdotante(List.of(Tag.ENERGICO));
		adotante.atualizarPerfil(
				List.of(Especie.GATO),
				List.of(Tag.ENERGICO),
				TipoMoradia.CASA,
				Porte.MEDIO,
				TempoDisponivel.ATE_30_MIN,
				ExperienciaAnimais.JA_TIVE_PETS,
				false,
				false,
				false,
				null,
				null
		);
		when(adotanteRepository.findById(adotanteId)).thenReturn(Optional.of(adotante));
		when(animalRepository.findByStatus(AnimalStatus.DISPONIVEL))
				.thenReturn(List.of(novoAnimal("Thor", List.of(Tag.ENERGICO))));

		List<AnimalRecomendadoResponse> recomendacoes = matchmakingService.recomendarAnimais(adotanteId);

		assertEquals(85, recomendacoes.getFirst().compatibilidade());
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
