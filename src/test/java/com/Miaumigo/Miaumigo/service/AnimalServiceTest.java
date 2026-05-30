package com.Miaumigo.Miaumigo.service;

import com.Miaumigo.Miaumigo.domain.Adotante;
import com.Miaumigo.Miaumigo.domain.Animal;
import com.Miaumigo.Miaumigo.domain.AnimalStatus;
import com.Miaumigo.Miaumigo.domain.Especie;
import com.Miaumigo.Miaumigo.domain.Porte;
import com.Miaumigo.Miaumigo.domain.SexoAnimal;
import com.Miaumigo.Miaumigo.domain.Tag;
import com.Miaumigo.Miaumigo.domain.Lar;
import com.Miaumigo.Miaumigo.domain.Operador;
import com.Miaumigo.Miaumigo.dto.AcaoRealizadaResponse;
import com.Miaumigo.Miaumigo.dto.AnimalResponse;
import com.Miaumigo.Miaumigo.dto.CadastroAnimalRequest;
import com.Miaumigo.Miaumigo.exception.RecursoNaoEncontradoException;
import com.Miaumigo.Miaumigo.repository.AdotanteRepository;
import com.Miaumigo.Miaumigo.repository.AnimalRepository;
import com.Miaumigo.Miaumigo.repository.OperadorRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AnimalServiceTest {

	private final AnimalRepository animalRepository = mock(AnimalRepository.class);
	private final AdotanteRepository adotanteRepository = mock(AdotanteRepository.class);
	private final OperadorRepository operadorRepository = mock(OperadorRepository.class);
	private final AnimalService animalService = new AnimalService(animalRepository, adotanteRepository, operadorRepository);

	@Test
	void deveCadastrarAnimal_quandoDadosValidos() {
		UUID larId = UUID.randomUUID();
		UUID operadorId = UUID.randomUUID();
		Lar lar = new Lar("Lar Amigo");
		ReflectionTestUtils.setField(lar, "id", larId);
		Operador operador = new Operador("Responsavel", "Rua A", "operador@email.com", "senha", "12345678901", lar);
		CadastroAnimalRequest request = new CadastroAnimalRequest(
				"Luna",
					Especie.GATO,
					Porte.PEQUENO,
					SexoAnimal.FEMEA,
					2,
					"Dócil e tranquila",
				List.of(Tag.DOCIL, Tag.CARINHOSO),
				"animais/luna"
		);
		when(operadorRepository.findById(operadorId)).thenReturn(Optional.of(operador));
		when(animalRepository.save(any(Animal.class))).thenAnswer(invocation -> invocation.getArgument(0));

		animalService.cadastrar(request, operadorId);

		ArgumentCaptor<Animal> animalCaptor = ArgumentCaptor.forClass(Animal.class);
		verify(animalRepository).save(animalCaptor.capture());
		Animal animalSalvo = animalCaptor.getValue();
		assertEquals("Luna", animalSalvo.getNome());
			assertEquals(Especie.GATO, animalSalvo.getEspecie());
			assertEquals(Porte.PEQUENO, animalSalvo.getPorte());
			assertEquals(SexoAnimal.FEMEA, animalSalvo.getSexo());
			assertEquals(2, animalSalvo.getIdade());
		assertEquals("Dócil e tranquila", animalSalvo.getDescricao());
		assertEquals(List.of(Tag.DOCIL, Tag.CARINHOSO), animalSalvo.getTags());
		assertEquals("animais/luna", animalSalvo.getCloudinaryPublicId());
		assertEquals(larId, animalSalvo.getLarId());
	}

	@Test
	void deveDevolverAnimal_quandoAnimalAdotado() {
		UUID id = UUID.randomUUID();
		Animal animal = new Animal("Luna", Especie.GATO, Porte.PEQUENO, 2, "Dócil", UUID.randomUUID());
		Adotante adotante = novoAdotante("Maria Silva");
		animal.realizarAdocao(adotante);
		when(animalRepository.findById(id)).thenReturn(Optional.of(animal));
		when(animalRepository.save(any(Animal.class))).thenAnswer(invocation -> invocation.getArgument(0));

		AcaoRealizadaResponse response = animalService.devolverAnimal(id, "Não se adaptou");

		ArgumentCaptor<Animal> animalCaptor = ArgumentCaptor.forClass(Animal.class);
		verify(animalRepository).save(animalCaptor.capture());
		assertEquals("Devolução registrada com sucesso.", response.mensagem());
		assertEquals(AnimalStatus.DISPONIVEL, animalCaptor.getValue().getStatus());
		assertEquals(null, animalCaptor.getValue().getAdotanteAtual());
		assertEquals("Devolveu Luna.", adotante.getLogs().getFirst());
	}

	@Test
	void deveRetornarAnimal_quandoIdValido() {
		UUID id = UUID.randomUUID();
		Animal animal = new Animal(
				"Luna",
					Especie.GATO,
					Porte.PEQUENO,
					SexoAnimal.FEMEA,
					2,
				"Dócil",
				UUID.randomUUID(),
				List.of(Tag.DOCIL, Tag.CARINHOSO),
				"animais/luna"
		);
		ReflectionTestUtils.setField(animal, "id", id);
		when(animalRepository.findById(id)).thenReturn(Optional.of(animal));

		AnimalResponse response = animalService.buscarPorId(id);

		assertEquals(animal.getId(), response.id());
		assertEquals("Luna", response.nome());
			assertEquals(2, response.idade());
			assertEquals(Porte.PEQUENO, response.porte());
			assertEquals(Especie.GATO, response.especie());
			assertEquals(SexoAnimal.FEMEA, response.sexo());
			assertEquals(List.of(Tag.DOCIL, Tag.CARINHOSO), response.tags());
		assertEquals("animais/luna", response.cloudinaryPublicId());
	}

	@Test
	void deveListarAnimaisDisponiveis_quandoExistiremAnimaisComStatusDisponivel() {
		UUID id = UUID.randomUUID();
		Animal animal = new Animal(
				"Luna",
					Especie.GATO,
					Porte.PEQUENO,
					SexoAnimal.FEMEA,
					2,
				"Dócil",
				UUID.randomUUID(),
				List.of(Tag.DOCIL, Tag.CARINHOSO),
				"animais/luna"
		);
		ReflectionTestUtils.setField(animal, "id", id);
		when(animalRepository.findByStatus(AnimalStatus.DISPONIVEL)).thenReturn(List.of(animal));

		List<AnimalResponse> response = animalService.listarDisponiveis();

		assertEquals(1, response.size());
			assertEquals(id, response.getFirst().id());
			assertEquals("Luna", response.getFirst().nome());
			assertEquals(SexoAnimal.FEMEA, response.getFirst().sexo());
			assertEquals(AnimalStatus.DISPONIVEL, response.getFirst().status());
		assertEquals("animais/luna", response.getFirst().cloudinaryPublicId());
		verify(animalRepository).findByStatus(AnimalStatus.DISPONIVEL);
	}

	@Test
	void deveRetornarListaVazia_quandoNaoExistiremAnimaisDisponiveis() {
		when(animalRepository.findByStatus(AnimalStatus.DISPONIVEL)).thenReturn(List.of());

		List<AnimalResponse> response = animalService.listarDisponiveis();

		assertTrue(response.isEmpty());
		verify(animalRepository).findByStatus(AnimalStatus.DISPONIVEL);
	}

	@Test
	void deveLancarExcecao_quandoAnimalNaoEncontrado() {
		UUID id = UUID.randomUUID();
		when(animalRepository.findById(id)).thenReturn(Optional.empty());

		RecursoNaoEncontradoException exception = assertThrows(
				RecursoNaoEncontradoException.class,
				() -> animalService.buscarPorId(id)
		);

		assertEquals("Animal não encontrado", exception.getMessage());
	}

	private Adotante novoAdotante(String nome) {
		return new Adotante(nome, "Rua das Flores, 123", nome.replace(" ", ".") + "@email.com", "senha123", "12345678901", List.of(Tag.DOCIL));
	}
}
