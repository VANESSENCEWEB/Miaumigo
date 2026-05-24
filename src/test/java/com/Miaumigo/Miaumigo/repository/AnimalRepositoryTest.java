package com.Miaumigo.Miaumigo.repository;

import com.Miaumigo.Miaumigo.domain.Animal;
import com.Miaumigo.Miaumigo.domain.AnimalStatus;
import com.Miaumigo.Miaumigo.domain.Adotante;
import com.Miaumigo.Miaumigo.domain.Especie;
import com.Miaumigo.Miaumigo.domain.Porte;
import com.Miaumigo.Miaumigo.domain.Tag;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class AnimalRepositoryTest {

	@Autowired
	private AnimalRepository animalRepository;

	@Autowired
	private AdotanteRepository adotanteRepository;

	@Autowired
	private EntityManager entityManager;

	@Test
	void deveSalvarAnimal_quandoDadosValidos() {
		Animal animal = new Animal(
				"Luna",
				Especie.GATO,
				Porte.PEQUENO,
				2,
				"Dócil",
				UUID.randomUUID(),
				List.of(Tag.DOCIL, Tag.CASTRADO),
				"animais/luna"
		);
		animal.adicionarLog("Recebeu vacina.");

		Animal animalSalvo = animalRepository.save(animal);

		Optional<Animal> animalEncontrado = animalRepository.findById(animalSalvo.getId());

		assertTrue(animalEncontrado.isPresent());
		assertEquals("Luna", animalEncontrado.get().getNome());
		assertEquals(AnimalStatus.DISPONIVEL, animalEncontrado.get().getStatus());
		assertEquals(List.of(Tag.DOCIL, Tag.CASTRADO), animalEncontrado.get().getTags());
		assertEquals("animais/luna", animalEncontrado.get().getCloudinaryPublicId());
		assertEquals(2, animalEncontrado.get().getLogs().size());
	}

	@Test
	void devePersistirAdotantes_quandoAnimalForAdotadoEDevolvido() {
		Adotante primeiroAdotante = adotanteRepository.save(novoAdotante("Maria Silva", "maria@email.com", "12345678901"));
		Adotante segundoAdotante = adotanteRepository.save(novoAdotante("João Souza", "joao@email.com", "98765432109"));
		Animal animal = new Animal("Luna", Especie.GATO, Porte.PEQUENO, 2, "Dócil", UUID.randomUUID());
		animal.realizarAdocao(primeiroAdotante);
		animal.devolver("Não se adaptou");
		animal.realizarAdocao(segundoAdotante);

		Animal animalSalvo = animalRepository.saveAndFlush(animal);
		entityManager.clear();
		Animal animalEncontrado = animalRepository.findById(animalSalvo.getId()).orElseThrow();

		assertEquals(AnimalStatus.ADOTADO, animalEncontrado.getStatus());
		assertEquals("João Souza", animalEncontrado.getAdotanteAtual().getNome());
		assertEquals(2, animalEncontrado.getAdotantes().size());
	}

	@Test
	void deveRetornarAnimais_quandoLarValido() {
		UUID larId = UUID.randomUUID();
		animalRepository.save(new Animal("Luna", Especie.GATO, Porte.PEQUENO, 2, "Dócil", larId));
		animalRepository.save(new Animal("Thor", Especie.CACHORRO, Porte.GRANDE, 4, "Brincalhão", UUID.randomUUID()));

		List<Animal> animais = animalRepository.findByLarId(larId);

		assertEquals(1, animais.size());
		assertEquals("Luna", animais.getFirst().getNome());
	}

	@Test
	void deveRetornarAnimais_quandoStatusValido() {
		Animal animalDisponivel = new Animal("Luna", Especie.GATO, Porte.PEQUENO, 2, "Dócil", UUID.randomUUID());
		Animal animalEmProcesso = new Animal("Thor", Especie.CACHORRO, Porte.GRANDE, 4, "Brincalhão", UUID.randomUUID());
		animalEmProcesso.iniciarProcessoAdocao();
		animalRepository.save(animalDisponivel);
		animalRepository.save(animalEmProcesso);

		List<Animal> animais = animalRepository.findByStatus(AnimalStatus.EM_PROCESSO);

		assertEquals(1, animais.size());
		assertEquals("Thor", animais.getFirst().getNome());
	}

	@Test
	void deveRetornarAnimais_quandoLarEStatusValidos() {
		UUID larId = UUID.randomUUID();
		Animal animalDisponivel = new Animal("Luna", Especie.GATO, Porte.PEQUENO, 2, "Dócil", larId);
		Animal animalEmProcesso = new Animal("Thor", Especie.CACHORRO, Porte.GRANDE, 4, "Brincalhão", larId);
		animalEmProcesso.iniciarProcessoAdocao();
		animalRepository.save(animalDisponivel);
		animalRepository.save(animalEmProcesso);

		List<Animal> animais = animalRepository.findByLarIdAndStatus(larId, AnimalStatus.DISPONIVEL);

		assertEquals(1, animais.size());
		assertEquals("Luna", animais.getFirst().getNome());
	}

	private Adotante novoAdotante(String nome, String email, String cpf) {
		return new Adotante(nome, "Rua das Flores, 123", email, "senha123", cpf, List.of(Tag.DOCIL));
	}
}
