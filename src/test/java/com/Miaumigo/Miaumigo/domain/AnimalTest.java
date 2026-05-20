package com.Miaumigo.Miaumigo.domain;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

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
		assertEquals(List.of(), animal.getTags());
		assertEquals(1, animal.getLogs().size());
		assertEquals("Animal cadastrado.", animal.getLogs().getFirst());
	}

	@Test
	void deveCriarAnimalComTagsEPublicIdCloudinary_quandoDadosValidos() {
		UUID larId = UUID.randomUUID();

		Animal animal = new Animal(
				"Luna",
				Especie.GATO,
				Porte.PEQUENO,
				2,
				"Dócil",
				larId,
				List.of("dócil", "castrada", "dócil", " "),
				" animais/luna "
		);

		assertEquals(List.of("dócil", "castrada"), animal.getTags());
		assertEquals("animais/luna", animal.getCloudinaryPublicId());
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
	void deveAdicionarLog_quandoMensagemValida() {
		Animal animal = new Animal("Luna", Especie.GATO, Porte.PEQUENO, 2, "Dócil", UUID.randomUUID());

		animal.adicionarLog("Recebeu vacina.");

		assertEquals(2, animal.getLogs().size());
		assertEquals("Recebeu vacina.", animal.getLogs().get(1));
	}

	@Test
	void deveLancarExcecao_quandoLogSemMensagem() {
		Animal animal = new Animal("Luna", Especie.GATO, Porte.PEQUENO, 2, "Dócil", UUID.randomUUID());

		assertThrows(IllegalArgumentException.class, () -> animal.adicionarLog(" "));
	}
}
