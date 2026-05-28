package com.Miaumigo.Miaumigo.security;

import com.Miaumigo.Miaumigo.exception.AcessoNegadoException;
import com.Miaumigo.Miaumigo.exception.IdentidadeNaoAutenticadaException;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UsuarioAutenticadoServiceTest {

	private final UsuarioAutenticadoService service = new UsuarioAutenticadoService();

	@Test
	void deveRetornarUsuarioId_quandoTokenForDeAdotante() {
		UUID usuarioId = UUID.randomUUID();

		UUID resultado = service.exigirAdotante(jwt(usuarioId.toString(), "ADOTANTE"));

		assertEquals(usuarioId, resultado);
	}

	@Test
	void deveLancarExcecao_quandoTokenForDeOutroPapel() {
		assertThrows(AcessoNegadoException.class,
				() -> service.exigirAdotante(jwt(UUID.randomUUID().toString(), "OPERADOR")));
	}

	@Test
	void deveRetornarUsuarioId_quandoTokenForDeOperador() {
		UUID usuarioId = UUID.randomUUID();

		UUID resultado = service.exigirOperador(jwt(usuarioId.toString(), "OPERADOR"));

		assertEquals(usuarioId, resultado);
	}

	@Test
	void deveLancarExcecao_quandoOperadorReceberTokenDeAdotante() {
		assertThrows(AcessoNegadoException.class,
				() -> service.exigirOperador(jwt(UUID.randomUUID().toString(), "ADOTANTE")));
	}

	@Test
	void deveRetornarUsuarioId_quandoTokenForDeAdmin() {
		UUID usuarioId = UUID.randomUUID();

		UUID resultado = service.exigirAdmin(jwt(usuarioId.toString(), "ADMIN"));

		assertEquals(usuarioId, resultado);
	}

	@Test
	void deveRetornarUsuarioId_quandoExigirOperadorOuAdminReceberAdmin() {
		UUID usuarioId = UUID.randomUUID();

		UUID resultado = service.exigirOperadorOuAdmin(jwt(usuarioId.toString(), "ADMIN"));

		assertEquals(usuarioId, resultado);
	}

	@Test
	void deveLancarExcecao_quandoOperadorOuAdminReceberTokenDeAdotante() {
		assertThrows(AcessoNegadoException.class,
				() -> service.exigirOperadorOuAdmin(jwt(UUID.randomUUID().toString(), "ADOTANTE")));
	}

	@Test
	void deveLancarExcecao_quandoTokenNaoTiverUsuarioIdValido() {
		assertThrows(IdentidadeNaoAutenticadaException.class,
				() -> service.exigirAdotante(jwt("invalido", "ADOTANTE")));
	}

	private Jwt jwt(String usuarioId, String papel) {
		return new Jwt(
				"token",
				Instant.now(),
				Instant.now().plusSeconds(3600),
				Map.of("alg", "none"),
				Map.of("usuario_id", usuarioId, "papel", papel)
		);
	}
}
