package com.Miaumigo.Miaumigo.service;

import com.Miaumigo.Miaumigo.domain.Animal;
import com.Miaumigo.Miaumigo.domain.Especie;
import com.Miaumigo.Miaumigo.domain.Porte;
import com.Miaumigo.Miaumigo.dto.AnimalResponse;
import com.Miaumigo.Miaumigo.dto.AtualizacaoAnimalRequest;
import com.Miaumigo.Miaumigo.dto.CadastroAnimalRequest;
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
		assertEquals(larId, animalSalvo.getLarId());
	}

	@Test
	void deveListarTodosAnimais_quandoExistiremAnimaisCadastrados() {
		UUID larId = UUID.randomUUID();
		Animal luna = new Animal("Luna", Especie.GATO, Porte.PEQUENO, 2, "Dócil e tranquila", larId);
		Animal thor = new Animal("Thor", Especie.CACHORRO, Porte.GRANDE, 4, "Brincalhão", larId);
		when(animalRepository.findAll()).thenReturn(List.of(luna, thor));

		List<AnimalResponse> animais = animalService.listarTodos();

		assertEquals(2, animais.size());
		assertEquals("Luna", animais.getFirst().nome());
		assertEquals(Especie.GATO, animais.getFirst().especie());
		assertEquals(Porte.PEQUENO, animais.getFirst().porte());
		assertEquals(2, animais.getFirst().idade());
		assertEquals("Dócil e tranquila", animais.getFirst().descricao());
		assertEquals(larId, animais.getFirst().larId());
		assertEquals("Thor", animais.get(1).nome());
		verify(animalRepository).findAll();
	}

	@Test
	void deveAtualizarAnimal_quandoIdValidoEDadosValidos() {
		UUID id = UUID.randomUUID();
		UUID larId = UUID.randomUUID();
		Animal animal = new Animal("Luna", Especie.GATO, Porte.PEQUENO, 2, "Dócil", larId);
		AtualizacaoAnimalRequest request = new AtualizacaoAnimalRequest(
				"Mel",
				Especie.CACHORRO,
				Porte.MEDIO,
				3,
				"Brincalhona"
		);
		when(animalRepository.findById(id)).thenReturn(Optional.of(animal));
		when(animalRepository.save(any(Animal.class))).thenAnswer(invocation -> invocation.getArgument(0));

		animalService.atualizar(id, request);

		ArgumentCaptor<Animal> animalCaptor = ArgumentCaptor.forClass(Animal.class);
		verify(animalRepository).save(animalCaptor.capture());
		Animal animalAtualizado = animalCaptor.getValue();
		assertEquals("Mel", animalAtualizado.getNome());
		assertEquals(Especie.CACHORRO, animalAtualizado.getEspecie());
		assertEquals(Porte.MEDIO, animalAtualizado.getPorte());
		assertEquals(3, animalAtualizado.getIdade());
		assertEquals("Brincalhona", animalAtualizado.getDescricao());
		assertEquals(larId, animalAtualizado.getLarId());
	}

	@Test
	void deveLancarExcecao_quandoAnimalNaoEncontradoParaAtualizacao() {
		UUID id = UUID.randomUUID();
		AtualizacaoAnimalRequest request = new AtualizacaoAnimalRequest(
				"Mel",
				Especie.CACHORRO,
				Porte.MEDIO,
				3,
				"Brincalhona"
		);
		when(animalRepository.findById(id)).thenReturn(Optional.empty());

		RecursoNaoEncontradoException exception = assertThrows(
				RecursoNaoEncontradoException.class,
				() -> animalService.atualizar(id, request)
		);

		assertEquals("Animal não encontrado.", exception.getMessage());
	}
}
