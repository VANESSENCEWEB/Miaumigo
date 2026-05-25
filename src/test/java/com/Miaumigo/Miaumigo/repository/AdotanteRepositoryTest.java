package com.Miaumigo.Miaumigo.repository;

import com.Miaumigo.Miaumigo.domain.Adotante;
import com.Miaumigo.Miaumigo.domain.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class AdotanteRepositoryTest {

	@Autowired
	private AdotanteRepository adotanteRepository;

	@Test
	void deveSalvarAdotante_quandoDadosValidos() {
		Adotante adotante = novoAdotante("Maria Silva", "maria@email.com", "12345678901");
		adotante.adicionarLog("Adotou Luna.");

		Adotante adotanteSalvo = adotanteRepository.saveAndFlush(adotante);

		Optional<Adotante> adotanteEncontrado = adotanteRepository.findById(adotanteSalvo.getId());

		assertTrue(adotanteEncontrado.isPresent());
		assertEquals("Maria Silva", adotanteEncontrado.get().getNome());
		assertEquals(List.of(Tag.CALMO, Tag.CONVIVE_COM_GATOS), adotanteEncontrado.get().getPreferencias());
		assertEquals(List.of("Adotou Luna."), adotanteEncontrado.get().getLogs());
	}

	@Test
	void deveLancarExcecao_quandoEmailDuplicado() {
		adotanteRepository.saveAndFlush(novoAdotante("Maria Silva", "maria@email.com", "12345678901"));

		assertThrows(DataIntegrityViolationException.class, () ->
				adotanteRepository.saveAndFlush(novoAdotante("João Souza", "maria@email.com", "98765432109")));
	}

	@Test
	void deveLancarExcecao_quandoCpfDuplicado() {
		adotanteRepository.saveAndFlush(novoAdotante("Maria Silva", "maria@email.com", "12345678901"));

		assertThrows(DataIntegrityViolationException.class, () ->
				adotanteRepository.saveAndFlush(novoAdotante("João Souza", "joao@email.com", "12345678901")));
	}

	private Adotante novoAdotante(String nome, String email, String cpf) {
		return new Adotante(nome, "Rua das Flores, 123", email, "senha123", cpf, List.of(Tag.CALMO, Tag.CONVIVE_COM_GATOS));
	}
}
