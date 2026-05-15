package com.Miaumigo.Miaumigo.domain;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AnimalTest {

	@Test
	void deveCriarAnimalDisponivel_quandoDadosValidos() {
		UUID larId = UUID.randomUUID();

		Animal animal = new Animal("Luna", Especie.GATO, Porte.PEQUENO, 2, "Dócil", larId);

		assertEquals("Luna", animal.getNome());
		assertEquals(AnimalStatus.DISPONIVEL, animal.getStatus());
		assertEquals(larId, animal.getLarId());
	}

	@Test
	void deveLancarExcecao_quandoNomeVazio() {
		assertThrows(IllegalArgumentException.class, () ->
				new Animal(" ", Especie.GATO, Porte.PEQUENO, 2, "Dócil", UUID.randomUUID()));
	}

	@Test
	void deveLancarExcecao_quandoIdadeNegativa() {
		assertThrows(IllegalArgumentException.class, () ->
				new Animal("Luna", Especie.GATO, Porte.PEQUENO, -1, "Dócil", UUID.randomUUID()));
	}

	@Test
	void deveLancarExcecao_quandoLarNulo() {
		assertThrows(IllegalArgumentException.class, () ->
				new Animal("Luna", Especie.GATO, Porte.PEQUENO, 2, "Dócil", null));
	}

	@Test
	void deveTransferirParaLar_quandoAnimalDisponivel() {
		Animal animal = new Animal("Luna", Especie.GATO, Porte.PEQUENO, 2, "Dócil", UUID.randomUUID());
		UUID novoLarId = UUID.randomUUID();

		animal.transferirParaLar(novoLarId);

		assertEquals(novoLarId, animal.getLarId());
	}

	@Test
	void deveLancarExcecao_quandoTransferirAnimalEmProcesso() {
		Animal animal = new Animal("Luna", Especie.GATO, Porte.PEQUENO, 2, "Dócil", UUID.randomUUID());
		animal.iniciarProcessoAdocao();

		assertThrows(IllegalStateException.class, () -> animal.transferirParaLar(UUID.randomUUID()));
	}

	@Test
	void devePermitirTransicaoParaEmProcesso_quandoAnimalDisponivel() {
		Animal animal = new Animal("Luna", Especie.GATO, Porte.PEQUENO, 2, "Dócil", UUID.randomUUID());

		animal.iniciarProcessoAdocao();

		assertEquals(AnimalStatus.EM_PROCESSO, animal.getStatus());
	}

	@Test
	void devePermitirTransicaoParaAdotado_quandoAnimalEmProcesso() {
		Animal animal = new Animal("Luna", Especie.GATO, Porte.PEQUENO, 2, "Dócil", UUID.randomUUID());
		animal.iniciarProcessoAdocao();

		animal.marcarComoAdotado();

		assertEquals(AnimalStatus.ADOTADO, animal.getStatus());
	}

	@Test
	void devePermitirRetornarParaDisponivel_quandoAnimalEmProcesso() {
		Animal animal = new Animal("Luna", Especie.GATO, Porte.PEQUENO, 2, "Dócil", UUID.randomUUID());
		animal.iniciarProcessoAdocao();

		animal.disponibilizar();

		assertEquals(AnimalStatus.DISPONIVEL, animal.getStatus());
	}

	@Test
	void devePermitirRetornarParaDisponivel_quandoAnimalAdotado() {
		Animal animal = new Animal("Luna", Especie.GATO, Porte.PEQUENO, 2, "Dócil", UUID.randomUUID());
		animal.iniciarProcessoAdocao();
		animal.marcarComoAdotado();

		animal.disponibilizar();

		assertEquals(AnimalStatus.DISPONIVEL, animal.getStatus());
	}

	@Test
	void deveLancarExcecao_quandoMarcarComoAdotadoAnimalDisponivel() {
		Animal animal = new Animal("Luna", Especie.GATO, Porte.PEQUENO, 2, "Dócil", UUID.randomUUID());

		assertThrows(IllegalStateException.class, animal::marcarComoAdotado);
	}

	@Test
	void deveAtualizarDados_quandoDadosValidos() {
		Animal animal = new Animal("Luna", Especie.GATO, Porte.PEQUENO, 2, "Dócil", UUID.randomUUID());

		assertDoesNotThrow(() -> animal.atualizarDados("Thor", Especie.CACHORRO, Porte.GRANDE, 4, "Brincalhão"));

		assertEquals("Thor", animal.getNome());
		assertEquals(Especie.CACHORRO, animal.getEspecie());
		assertEquals(Porte.GRANDE, animal.getPorte());
	}
}
