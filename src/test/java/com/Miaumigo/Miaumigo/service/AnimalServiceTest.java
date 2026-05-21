package com.Miaumigo.Miaumigo.service;

import com.Miaumigo.Miaumigo.domain.Animal;
import com.Miaumigo.Miaumigo.domain.AnimalStatus;
import com.Miaumigo.Miaumigo.domain.Especie;
import com.Miaumigo.Miaumigo.domain.Porte;
import com.Miaumigo.Miaumigo.dto.AcaoRealizadaResponse;
import com.Miaumigo.Miaumigo.dto.CadastroAnimalRequest;
import com.Miaumigo.Miaumigo.dto.RealizarAdocaoRequest;
import com.Miaumigo.Miaumigo.exception.RecursoNaoEncontradoException;
import com.Miaumigo.Miaumigo.repository.AnimalRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AnimalServiceTest {

	private final AnimalRepository animalRepository = mock(AnimalRepository.class);
	private final AnimalService animalService = new AnimalService(animalRepository);

	@Test
	void deveCadastrarAnimal_quandoDadosValidos() {
		UUID larId = UUID.randomUUID();
		CadastroAnimalRequest request = new CadastroAnimalRequest(
				"Luna",
				Especie.GATO,
				Porte.PEQUENO,
				2,
				"Dócil e tranquila",
				List.of("dócil", "castrada"),
				"animais/luna",
				larId
		);
		when(animalRepository.save(any(Animal.class))).thenAnswer(invocation -> invocation.getArgument(0));

		animalService.cadastrar(request);

		ArgumentCaptor<Animal> animalCaptor = ArgumentCaptor.forClass(Animal.class);
		verify(animalRepository).save(animalCaptor.capture());
		Animal animalSalvo = animalCaptor.getValue();
		assertEquals("Luna", animalSalvo.getNome());
		assertEquals(Especie.GATO, animalSalvo.getEspecie());
		assertEquals(Porte.PEQUENO, animalSalvo.getPorte());
		assertEquals(2, animalSalvo.getIdade());
		assertEquals("Dócil e tranquila", animalSalvo.getDescricao());
		assertEquals(List.of("dócil", "castrada"), animalSalvo.getTags());
		assertEquals("animais/luna", animalSalvo.getCloudinaryPublicId());
		assertEquals(larId, animalSalvo.getLarId());
	}

	@Test
	void deveRealizarAdocao_quandoAnimalExistir() {
		UUID id = UUID.randomUUID();
		Animal animal = new Animal("Luna", Especie.GATO, Porte.PEQUENO, 2, "Dócil", UUID.randomUUID());
		RealizarAdocaoRequest request = new RealizarAdocaoRequest("Maria Silva");
		when(animalRepository.findById(id)).thenReturn(Optional.of(animal));
		when(animalRepository.save(any(Animal.class))).thenAnswer(invocation -> invocation.getArgument(0));

		AcaoRealizadaResponse response = animalService.realizarAdocao(id, request);

		ArgumentCaptor<Animal> animalCaptor = ArgumentCaptor.forClass(Animal.class);
		verify(animalRepository).save(animalCaptor.capture());
		Animal animalSalvo = animalCaptor.getValue();
		assertEquals("Adoção realizada com sucesso.", response.mensagem());
		assertEquals(AnimalStatus.ADOTADO, animalSalvo.getStatus());
		assertEquals("Adotado por Maria Silva.", animalSalvo.getLogs().get(1));
	}

	@Test
	void deveLancarExcecao_quandoAnimalNaoEncontradoParaAdocao() {
		UUID id = UUID.randomUUID();
		RealizarAdocaoRequest request = new RealizarAdocaoRequest("Maria Silva");
		when(animalRepository.findById(id)).thenReturn(Optional.empty());

		RecursoNaoEncontradoException exception = assertThrows(
				RecursoNaoEncontradoException.class,
				() -> animalService.realizarAdocao(id, request)
		);

		assertEquals("Animal não encontrado.", exception.getMessage());
	}
}
