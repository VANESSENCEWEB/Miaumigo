package com.Miaumigo.Miaumigo.service;

import com.Miaumigo.Miaumigo.domain.Adotante;
import com.Miaumigo.Miaumigo.domain.Animal;
import com.Miaumigo.Miaumigo.domain.AnimalStatus;
import com.Miaumigo.Miaumigo.domain.Especie;
import com.Miaumigo.Miaumigo.domain.Lar;
import com.Miaumigo.Miaumigo.domain.Operador;
import com.Miaumigo.Miaumigo.domain.Porte;
import com.Miaumigo.Miaumigo.domain.SolicitacaoStatus;
import com.Miaumigo.Miaumigo.repository.AdotanteRepository;
import com.Miaumigo.Miaumigo.repository.AnimalRepository;
import com.Miaumigo.Miaumigo.repository.LarRepository;
import com.Miaumigo.Miaumigo.repository.OperadorRepository;
import com.Miaumigo.Miaumigo.repository.SolicitacaoAdocaoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
class SolicitacaoAdocaoFluxoTest {

	@Autowired
	private SolicitacaoAdocaoService solicitacaoService;

	@Autowired
	private LarRepository larRepository;

	@Autowired
	private OperadorRepository operadorRepository;

	@Autowired
	private AnimalRepository animalRepository;

	@Autowired
	private AdotanteRepository adotanteRepository;

	@Autowired
	private SolicitacaoAdocaoRepository solicitacaoRepository;

	@Test
	void deveAprovarUmCandidatoERejeitarOutro_quandoExistiremSolicitacoesConcorrentes() {
		Lar lar = larRepository.save(new Lar("Lar Amigo"));
		Operador operador = operadorRepository.save(new Operador(
				"Ana", "Rua Lar", "ana@email.com", "senha", "11122233344", lar
		));
		Animal animal = animalRepository.save(new Animal(
				"Luna", Especie.GATO, Porte.PEQUENO, 2, "Calma", lar.getId()
		));
		Adotante maria = adotanteRepository.save(novoAdotante("Maria", "maria@email.com", "12345678901"));
		Adotante joao = adotanteRepository.save(novoAdotante("Joao", "joao@email.com", "98765432109"));

		var selecionada = solicitacaoService.criar(animal.getId(), maria.getId());
		solicitacaoService.criar(animal.getId(), joao.getId());

		solicitacaoService.aprovar(selecionada.id(), operador.getId());

		assertEquals(AnimalStatus.ADOTADO, animalRepository.findById(animal.getId()).orElseThrow().getStatus());
		assertEquals(1, solicitacaoRepository.findByAnimalIdAndStatus(animal.getId(), SolicitacaoStatus.APROVADA).size());
		assertEquals(1, solicitacaoRepository.findByAnimalIdAndStatus(animal.getId(), SolicitacaoStatus.REJEITADA).size());
	}

	private Adotante novoAdotante(String nome, String email, String cpf) {
		return new Adotante(nome, "Rua A", email, "senha", cpf, List.of());
	}
}
