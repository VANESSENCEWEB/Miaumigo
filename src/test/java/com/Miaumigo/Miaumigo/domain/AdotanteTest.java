package com.Miaumigo.Miaumigo.domain;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AdotanteTest {

	@Test
	void deveCriarAdotante_quandoDadosValidos() {
		Adotante adotante = novoAdotante(List.of(Tag.DOCIL, Tag.CARINHOSO));

		assertEquals("Maria Silva", adotante.getNome());
		assertEquals(List.of(Tag.DOCIL, Tag.CARINHOSO), adotante.getPreferencias());
		assertEquals(List.of(), adotante.getLogs());
	}

	@Test
	void deveNormalizarPreferencias_quandoPossuiDuplicadasOuNulas() {
		Adotante adotante = novoAdotante(Arrays.asList(Tag.DOCIL, null, Tag.CARINHOSO, Tag.DOCIL));

		assertEquals(List.of(Tag.DOCIL, Tag.CARINHOSO), adotante.getPreferencias());
	}

	@Test
	void deveCriarAdotanteSemPreferencias_quandoPreferenciasNulas() {
		Adotante adotante = novoAdotante(null);

		assertEquals(List.of(), adotante.getPreferencias());
	}

	@Test
	void deveAdicionarLog_quandoMensagemValida() {
		Adotante adotante = novoAdotante(List.of());

		adotante.adicionarLog(" Adotou Luna. ");

		assertEquals(List.of("Adotou Luna."), adotante.getLogs());
	}

	@Test
	void deveLancarExcecao_quandoLogSemMensagem() {
		Adotante adotante = novoAdotante(List.of());

		assertThrows(IllegalArgumentException.class, () -> adotante.adicionarLog(" "));
	}

	private Adotante novoAdotante(List<Tag> preferencias) {
		return new Adotante(
				"Maria Silva",
				"Rua das Flores, 123",
				"maria@email.com",
				"senha123",
				"12345678901",
				preferencias
		);
	}
}
