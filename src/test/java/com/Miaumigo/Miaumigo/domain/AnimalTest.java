package com.Miaumigo.Miaumigo.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
				Arrays.asList(Tag.DOCIL, Tag.CARINHOSO, Tag.DOCIL, null),
				" animais/luna "
		);

		assertEquals(List.of(Tag.DOCIL, Tag.CARINHOSO), animal.getTags());
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
	void deveRealizarAdocao_quandoAnimalDisponivel() {
		Animal animal = new Animal("Luna", Especie.GATO, Porte.PEQUENO, 2, "Dócil", UUID.randomUUID());
		Adotante adotante = novoAdotante("Maria Silva");

		animal.realizarAdocao(adotante);

		assertEquals(AnimalStatus.ADOTADO, animal.getStatus());
		assertEquals(adotante, animal.getAdotanteAtual());
		assertTrue(animal.getAdotantes().contains(adotante));
		assertEquals("Adotado por Maria Silva.", animal.getLogs().get(1));
	}

	@Test
	void deveLancarExcecao_quandoRealizarAdocaoAnimalIndisponivel() {
		Animal animal = new Animal("Luna", Especie.GATO, Porte.PEQUENO, 2, "Dócil", UUID.randomUUID());
		animal.iniciarProcessoAdocao();

		assertThrows(IllegalStateException.class, () -> animal.realizarAdocao(novoAdotante("Maria Silva")));
	}

	@Test
	void deveLancarExcecao_quandoRealizarAdocaoSemAdotante() {
		Animal animal = new Animal("Luna", Especie.GATO, Porte.PEQUENO, 2, "Dócil", UUID.randomUUID());

		assertThrows(IllegalArgumentException.class, () -> animal.realizarAdocao(null));
	}

	@Test
	void deveDevolverAnimal_quandoAnimalAdotado() {
		Animal animal = new Animal("Luna", Especie.GATO, Porte.PEQUENO, 2, "Dócil", UUID.randomUUID());
		Adotante adotante = novoAdotante("Maria Silva");
		animal.realizarAdocao(adotante);

		Adotante adotanteAnterior = animal.devolver("Não se adaptou");

		assertEquals(adotante, adotanteAnterior);
		assertEquals(AnimalStatus.DISPONIVEL, animal.getStatus());
		assertEquals(null, animal.getAdotanteAtual());
		assertTrue(animal.getAdotantes().contains(adotante));
		assertEquals("Animal devolvido por Maria Silva. Motivo: Não se adaptou.", animal.getLogs().get(2));
	}

	@Test
	void devePermitirNovaAdocao_quandoAnimalFoiDevolvido() {
		Animal animal = new Animal("Luna", Especie.GATO, Porte.PEQUENO, 2, "Dócil", UUID.randomUUID());
		Adotante primeiroAdotante = novoAdotante("Maria Silva");
		Adotante segundoAdotante = novoAdotante("João Souza");
		animal.realizarAdocao(primeiroAdotante);
		animal.devolver(null);

		animal.realizarAdocao(segundoAdotante);

		assertEquals(AnimalStatus.ADOTADO, animal.getStatus());
		assertEquals(segundoAdotante, animal.getAdotanteAtual());
		assertTrue(animal.getAdotantes().contains(primeiroAdotante));
		assertTrue(animal.getAdotantes().contains(segundoAdotante));
	}

	@Test
	void deveLancarExcecao_quandoDevolverAnimalNaoAdotado() {
		Animal animal = new Animal("Luna", Especie.GATO, Porte.PEQUENO, 2, "Dócil", UUID.randomUUID());

		assertThrows(IllegalStateException.class, () -> animal.devolver("Não se adaptou"));
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

	@Test
	void deveAtualizarDataAtualizadoEm_quandoTransferirParaLar() {
		Animal animal = new Animal("Luna", Especie.GATO, Porte.PEQUENO, 2, "Dócil", UUID.randomUUID());

		assertAtualizadoEmFoiAtualizado(animal, a -> a.transferirParaLar(UUID.randomUUID()));
	}

	@Test
	void deveAtualizarDataAtualizadoEm_quandoDisponibilizar() {
		Animal animal = new Animal("Luna", Especie.GATO, Porte.PEQUENO, 2, "Dócil", UUID.randomUUID());
		animal.iniciarProcessoAdocao();

		assertAtualizadoEmFoiAtualizado(animal, Animal::disponibilizar);
	}

	@Test
	void deveAtualizarDataAtualizadoEm_quandoIniciarProcessoAdocao() {
		Animal animal = new Animal("Luna", Especie.GATO, Porte.PEQUENO, 2, "Dócil", UUID.randomUUID());

		assertAtualizadoEmFoiAtualizado(animal, Animal::iniciarProcessoAdocao);
	}

	@Test
	void deveAtualizarDataAtualizadoEm_quandoMarcarComoAdotado() {
		Animal animal = new Animal("Luna", Especie.GATO, Porte.PEQUENO, 2, "Dócil", UUID.randomUUID());
		animal.iniciarProcessoAdocao();

		assertAtualizadoEmFoiAtualizado(animal, Animal::marcarComoAdotado);
	}

	@Test
	void deveAtualizarDataAtualizadoEm_quandoRealizarAdocao() {
		Animal animal = new Animal("Luna", Especie.GATO, Porte.PEQUENO, 2, "Dócil", UUID.randomUUID());

		assertAtualizadoEmFoiAtualizado(animal, a -> a.realizarAdocao(novoAdotante("Maria Silva")));
	}

	@Test
	void deveAtualizarDataAtualizadoEm_quandoDevolver() {
		Animal animal = new Animal("Luna", Especie.GATO, Porte.PEQUENO, 2, "Dócil", UUID.randomUUID());
		animal.realizarAdocao(novoAdotante("Maria Silva"));

		assertAtualizadoEmFoiAtualizado(animal, a -> a.devolver("Não se adaptou"));
	}

	@Test
	void deveAtualizarDataAtualizadoEm_quandoAdicionarLog() {
		Animal animal = new Animal("Luna", Especie.GATO, Porte.PEQUENO, 2, "Dócil", UUID.randomUUID());

		assertAtualizadoEmFoiAtualizado(animal, a -> a.adicionarLog("Recebeu vacina."));
	}

	private void assertAtualizadoEmFoiAtualizado(Animal animal, Consumer<Animal> acao) {
		LocalDateTime atualizadoEmAnterior = animal.getAtualizadoEm();
		aguardarProximoInstante();

		acao.accept(animal);

		assertTrue(animal.getAtualizadoEm().isAfter(atualizadoEmAnterior));
	}

	private void aguardarProximoInstante() {
		try {
			Thread.sleep(5);
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("Teste interrompido.", exception);
		}
	}

	private Adotante novoAdotante(String nome) {
		return new Adotante(nome, "Rua das Flores, 123", nome.replace(" ", ".") + "@email.com", "senha123", "12345678901", List.of());
	}
}
