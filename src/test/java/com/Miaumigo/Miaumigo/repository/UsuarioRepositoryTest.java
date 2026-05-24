package com.Miaumigo.Miaumigo.repository;

import com.Miaumigo.Miaumigo.domain.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class UsuarioRepositoryTest {

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Test
	void deveSalvarUsuario_quandoDadosValidos() {
		Usuario usuario = novoUsuario("Maria Silva", "maria@email.com", "12345678901");

		Usuario usuarioSalvo = usuarioRepository.save(usuario);

		Optional<Usuario> usuarioEncontrado = usuarioRepository.findById(usuarioSalvo.getId());

		assertTrue(usuarioEncontrado.isPresent());
		assertEquals("Maria Silva", usuarioEncontrado.get().getNome());
		assertEquals("maria@email.com", usuarioEncontrado.get().getEmail());
		assertEquals("12345678901", usuarioEncontrado.get().getCpf());
	}

	@Test
	void deveRetornarUsuario_quandoEmailValido() {
		usuarioRepository.save(novoUsuario("Maria Silva", "maria@email.com", "12345678901"));

		Optional<Usuario> usuario = usuarioRepository.findByEmail("maria@email.com");

		assertTrue(usuario.isPresent());
		assertEquals("Maria Silva", usuario.get().getNome());
		assertTrue(usuarioRepository.existsByEmail("maria@email.com"));
	}

	@Test
	void deveRetornarUsuario_quandoCpfValido() {
		usuarioRepository.save(novoUsuario("Maria Silva", "maria@email.com", "12345678901"));

		Optional<Usuario> usuario = usuarioRepository.findByCpf("12345678901");

		assertTrue(usuario.isPresent());
		assertEquals("Maria Silva", usuario.get().getNome());
		assertTrue(usuarioRepository.existsByCpf("12345678901"));
	}

	@Test
	void deveLancarExcecao_quandoEmailDuplicado() {
		usuarioRepository.saveAndFlush(novoUsuario("Maria Silva", "maria@email.com", "12345678901"));

		assertThrows(DataIntegrityViolationException.class, () ->
				usuarioRepository.saveAndFlush(novoUsuario("João Souza", "maria@email.com", "98765432109")));
	}

	@Test
	void deveLancarExcecao_quandoCpfDuplicado() {
		usuarioRepository.saveAndFlush(novoUsuario("Maria Silva", "maria@email.com", "12345678901"));

		assertThrows(DataIntegrityViolationException.class, () ->
				usuarioRepository.saveAndFlush(novoUsuario("João Souza", "joao@email.com", "12345678901")));
	}

	private Usuario novoUsuario(String nome, String email, String cpf) {
		return new Usuario(nome, "Rua das Flores, 123", " " + email + " ", "senha123", cpf);
	}
}
