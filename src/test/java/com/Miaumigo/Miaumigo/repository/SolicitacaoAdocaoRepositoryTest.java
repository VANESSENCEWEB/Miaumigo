package com.Miaumigo.Miaumigo.repository;

import com.Miaumigo.Miaumigo.domain.Adotante;
import com.Miaumigo.Miaumigo.domain.Animal;
import com.Miaumigo.Miaumigo.domain.Especie;
import com.Miaumigo.Miaumigo.domain.Lar;
import com.Miaumigo.Miaumigo.domain.Porte;
import com.Miaumigo.Miaumigo.domain.SolicitacaoAdocao;
import com.Miaumigo.Miaumigo.domain.SolicitacaoStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class SolicitacaoAdocaoRepositoryTest {

	@Autowired
	private SolicitacaoAdocaoRepository solicitacaoRepository;

	@Autowired
	private AnimalRepository animalRepository;

	@Autowired
	private AdotanteRepository adotanteRepository;

	@Autowired
	private LarRepository larRepository;

	@Test
	void deveConsultarSolicitacoesPendentes_quandoLarEAdotantesValidos() {
		Lar lar = larRepository.save(new Lar("Lar Amigo"));
		Animal animal = animalRepository.save(new Animal("Luna", Especie.GATO, Porte.PEQUENO, 2, "Calma", lar.getId()));
		Adotante maria = adotanteRepository.save(novoAdotante("Maria", "maria@email.com", "12345678901"));
		Adotante joao = adotanteRepository.save(novoAdotante("Joao", "joao@email.com", "98765432109"));
		solicitacaoRepository.save(new SolicitacaoAdocao(animal, maria));
		solicitacaoRepository.save(new SolicitacaoAdocao(animal, joao));

		assertTrue(solicitacaoRepository.existsByAnimalIdAndAdotanteIdAndStatus(
				animal.getId(), maria.getId(), SolicitacaoStatus.PENDENTE));
		assertEquals(2, solicitacaoRepository.findByAnimalLarIdAndStatus(
				lar.getId(), SolicitacaoStatus.PENDENTE).size());
		assertEquals(1, solicitacaoRepository.findByAdotanteIdOrderByCriadoEmDesc(maria.getId()).size());
	}

	private Adotante novoAdotante(String nome, String email, String cpf) {
		return new Adotante(nome, "Rua A", email, "senha", cpf, List.of());
	}
}
