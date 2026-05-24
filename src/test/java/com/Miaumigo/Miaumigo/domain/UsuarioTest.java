package com.Miaumigo.Miaumigo.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UsuarioTest {

	@Test
	void deveCriarUsuario_quandoDadosValidos() {
		Usuario usuario = new Usuario(
				"Maria Silva",
				"Rua das Flores, 123",
				"maria@email.com",
				"senha123",
				"12345678901"
		);

		assertEquals("Maria Silva", usuario.getNome());
		assertEquals("Rua das Flores, 123", usuario.getEndereco());
		assertEquals("maria@email.com", usuario.getEmail());
		assertEquals("senha123", usuario.getSenha());
		assertEquals("12345678901", usuario.getCpf());
	}

	@Test
	void deveNormalizarDados_quandoDadosPossuemEspacosMaiusculasOuPontuacao() {
		Usuario usuario = new Usuario(
				" Maria Silva ",
				" Rua das Flores, 123 ",
				" MARIA@EMAIL.COM ",
				" senha123 ",
				"123.456.789-01"
		);

		assertEquals("Maria Silva", usuario.getNome());
		assertEquals("Rua das Flores, 123", usuario.getEndereco());
		assertEquals("maria@email.com", usuario.getEmail());
		assertEquals("senha123", usuario.getSenha());
		assertEquals("12345678901", usuario.getCpf());
	}

	@Test
	void deveLancarExcecao_quandoNomeVazio() {
		assertThrows(IllegalArgumentException.class, () ->
				new Usuario(" ", "Rua das Flores, 123", "maria@email.com", "senha123", "12345678901"));
	}

	@Test
	void deveLancarExcecao_quandoEnderecoVazio() {
		assertThrows(IllegalArgumentException.class, () ->
				new Usuario("Maria Silva", " ", "maria@email.com", "senha123", "12345678901"));
	}

	@Test
	void deveLancarExcecao_quandoEmailVazio() {
		assertThrows(IllegalArgumentException.class, () ->
				new Usuario("Maria Silva", "Rua das Flores, 123", " ", "senha123", "12345678901"));
	}

	@Test
	void deveLancarExcecao_quandoSenhaVazia() {
		assertThrows(IllegalArgumentException.class, () ->
				new Usuario("Maria Silva", "Rua das Flores, 123", "maria@email.com", " ", "12345678901"));
	}

	@Test
	void deveLancarExcecao_quandoCpfVazio() {
		assertThrows(IllegalArgumentException.class, () ->
				new Usuario("Maria Silva", "Rua das Flores, 123", "maria@email.com", "senha123", " "));
	}

	@Test
	void deveLancarExcecao_quandoCpfNaoPossuiOnzeDigitos() {
		assertThrows(IllegalArgumentException.class, () ->
				new Usuario("Maria Silva", "Rua das Flores, 123", "maria@email.com", "senha123", "1234567890"));
	}
}
