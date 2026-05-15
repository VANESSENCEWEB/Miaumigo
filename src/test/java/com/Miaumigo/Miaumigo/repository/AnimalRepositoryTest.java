package com.Miaumigo.Miaumigo.repository;

import com.Miaumigo.Miaumigo.domain.Animal;
import com.Miaumigo.Miaumigo.domain.AnimalStatus;
import com.Miaumigo.Miaumigo.domain.Especie;
import com.Miaumigo.Miaumigo.domain.Porte;
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

	@Test
	void deveSalvarAnimal_quandoDadosValidos() {
		Animal animal = new Animal("Luna", Especie.GATO, Porte.PEQUENO, 2, "Dócil", UUID.randomUUID());

		Animal animalSalvo = animalRepository.save(animal);

		Optional<Animal> animalEncontrado = animalRepository.findById(animalSalvo.getId());

		assertTrue(animalEncontrado.isPresent());
		assertEquals("Luna", animalEncontrado.get().getNome());
		assertEquals(AnimalStatus.DISPONIVEL, animalEncontrado.get().getStatus());
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
}
