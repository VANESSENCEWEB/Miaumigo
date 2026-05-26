package com.Miaumigo.Miaumigo.domain;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SolicitacaoAdocaoTest {

	@Test
	void deveCriarSolicitacaoPendente_quandoAnimalEAdotanteValidos() {
		SolicitacaoAdocao solicitacao = new SolicitacaoAdocao(novoAnimal(), novoAdotante());

		assertEquals(SolicitacaoStatus.PENDENTE, solicitacao.getStatus());
		assertEquals("Luna", solicitacao.getAnimal().getNome());
		assertEquals("Maria Silva", solicitacao.getAdotante().getNome());
	}

	@Test
	void deveAprovarSolicitacao_quandoPendente() {
		Operador operador = novoOperador();
		SolicitacaoAdocao solicitacao = new SolicitacaoAdocao(novoAnimal(), novoAdotante());

		solicitacao.aprovar(operador);

		assertEquals(SolicitacaoStatus.APROVADA, solicitacao.getStatus());
		assertEquals(operador, solicitacao.getOperadorDecisao());
	}

	@Test
	void deveLancarExcecao_quandoCancelarSolicitacaoDeOutroAdotante() {
		SolicitacaoAdocao solicitacao = new SolicitacaoAdocao(novoAnimal(), novoAdotante());

		assertThrows(IllegalArgumentException.class, () -> solicitacao.cancelar(
				new Adotante("Outro", "Rua B", "outro@email.com", "senha", "98765432109", List.of())
		));
	}

	private Animal novoAnimal() {
		return new Animal("Luna", Especie.GATO, Porte.PEQUENO, 2, "Calma", UUID.randomUUID());
	}

	private Adotante novoAdotante() {
		return new Adotante("Maria Silva", "Rua A", "maria@email.com", "senha", "12345678901", List.of());
	}

	private Operador novoOperador() {
		return new Operador(
				"Responsavel",
				"Rua Lar",
				"operador@email.com",
				"senha",
				"11122233344",
				new Lar("Lar Amigo")
		);
	}
}
