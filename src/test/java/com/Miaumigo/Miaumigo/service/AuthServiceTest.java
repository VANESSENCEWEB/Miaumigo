package com.Miaumigo.Miaumigo.service;

import com.Miaumigo.Miaumigo.domain.Adotante;
import com.Miaumigo.Miaumigo.domain.Lar;
import com.Miaumigo.Miaumigo.domain.Operador;
import com.Miaumigo.Miaumigo.dto.LoginRequest;
import com.Miaumigo.Miaumigo.dto.LoginResponse;
import com.Miaumigo.Miaumigo.exception.CredenciaisInvalidasException;
import com.Miaumigo.Miaumigo.repository.UsuarioRepository;
import com.Miaumigo.Miaumigo.security.JwtService;
import com.Miaumigo.Miaumigo.security.JwtService.TokenGerado;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class AuthServiceTest {

	private final UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
	private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
	private final JwtService jwtService = mock(JwtService.class);
	private final AuthService authService = new AuthService(usuarioRepository, passwordEncoder, jwtService);

	@Test
	void deveAutenticarAdotante_quandoSenhaBCryptValida() {
		UUID id = UUID.randomUUID();
		Adotante adotante = novoAdotante(id, passwordEncoder.encode("senha123"));
		Instant expiraEm = Instant.parse("2026-05-27T12:00:00Z");
		when(usuarioRepository.findByEmail("maria@email.com")).thenReturn(Optional.of(adotante));
		when(jwtService.gerarToken(id, "maria@email.com", "ADOTANTE"))
				.thenReturn(new TokenGerado("jwt-token", expiraEm));

		LoginResponse response = authService.login(new LoginRequest(" Maria@Email.com ", "senha123"));

		assertEquals("jwt-token", response.accessToken());
		assertEquals("Bearer", response.tokenType());
		assertEquals(expiraEm, response.expiraEm());
		assertEquals(id, response.usuario().id());
		assertEquals("Maria Silva", response.usuario().nome());
		assertEquals("maria@email.com", response.usuario().email());
		assertEquals("ADOTANTE", response.usuario().papel());
		verify(jwtService).gerarToken(id, "maria@email.com", "ADOTANTE");
	}

	@Test
	void deveAutenticarAdotante_quandoSenhaLegadaEmTextoPuro() {
		UUID id = UUID.randomUUID();
		Adotante adotante = novoAdotante(id, "senha123");
		Instant expiraEm = Instant.parse("2026-05-27T12:00:00Z");
		when(usuarioRepository.findByEmail("maria@email.com")).thenReturn(Optional.of(adotante));
		when(jwtService.gerarToken(id, "maria@email.com", "ADOTANTE"))
				.thenReturn(new TokenGerado("jwt-token", expiraEm));

		LoginResponse response = authService.login(new LoginRequest("maria@email.com", "senha123"));

		assertEquals("jwt-token", response.accessToken());
		assertEquals(expiraEm, response.expiraEm());
	}

	@Test
	void deveLancarExcecao_quandoEmailNaoExistir() {
		when(usuarioRepository.findByEmail("maria@email.com")).thenReturn(Optional.empty());

		assertThrows(CredenciaisInvalidasException.class,
				() -> authService.login(new LoginRequest("maria@email.com", "senha123")));
		verifyNoInteractions(jwtService);
	}

	@Test
	void deveLancarExcecao_quandoSenhaInvalida() {
		Adotante adotante = novoAdotante(UUID.randomUUID(), passwordEncoder.encode("senha123"));
		when(usuarioRepository.findByEmail("maria@email.com")).thenReturn(Optional.of(adotante));

		assertThrows(CredenciaisInvalidasException.class,
				() -> authService.login(new LoginRequest("maria@email.com", "outra-senha")));
		verifyNoInteractions(jwtService);
	}

	@Test
	void deveAutenticarOperador_quandoSenhaValida() {
		UUID id = UUID.randomUUID();
		Lar lar = new Lar("Lar Amigo");
		Operador operador = new Operador("Ana", "Rua A", "ana@email.com", passwordEncoder.encode("senha123"), "12345678901", lar);
		ReflectionTestUtils.setField(operador, "id", id);
		Instant expiraEm = Instant.parse("2026-05-27T12:00:00Z");
		when(usuarioRepository.findByEmail("ana@email.com")).thenReturn(Optional.of(operador));
		when(jwtService.gerarToken(id, "ana@email.com", "OPERADOR"))
				.thenReturn(new TokenGerado("jwt-token", expiraEm));

		LoginResponse response = authService.login(new LoginRequest("ana@email.com", "senha123"));

		assertEquals("jwt-token", response.accessToken());
		assertEquals(id, response.usuario().id());
		assertEquals("Ana", response.usuario().nome());
		assertEquals("ana@email.com", response.usuario().email());
		assertEquals("OPERADOR", response.usuario().papel());
		verify(jwtService).gerarToken(id, "ana@email.com", "OPERADOR");
	}

	private Adotante novoAdotante(UUID id, String senha) {
		Adotante adotante = new Adotante(
				"Maria Silva",
				"Rua das Flores, 123",
				"maria@email.com",
				senha,
				"12345678901",
				List.of()
		);
		ReflectionTestUtils.setField(adotante, "id", id);
		return adotante;
	}
}
