package com.Miaumigo.Miaumigo.service;

import com.Miaumigo.Miaumigo.domain.Adotante;
import com.Miaumigo.Miaumigo.domain.Animal;
import com.Miaumigo.Miaumigo.domain.AnimalStatus;
import com.Miaumigo.Miaumigo.domain.Especie;
import com.Miaumigo.Miaumigo.domain.Lar;
import com.Miaumigo.Miaumigo.domain.Operador;
import com.Miaumigo.Miaumigo.domain.Porte;
import com.Miaumigo.Miaumigo.domain.SolicitacaoAdocao;
import com.Miaumigo.Miaumigo.domain.SolicitacaoStatus;
import com.Miaumigo.Miaumigo.dto.SolicitacaoAdocaoResponse;
import com.Miaumigo.Miaumigo.repository.AdotanteRepository;
import com.Miaumigo.Miaumigo.repository.AnimalRepository;
import com.Miaumigo.Miaumigo.repository.OperadorRepository;
import com.Miaumigo.Miaumigo.repository.SolicitacaoAdocaoRepository;
import com.Miaumigo.Miaumigo.exception.AcessoNegadoException;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SolicitacaoAdocaoServiceTest {

	private final SolicitacaoAdocaoRepository solicitacaoRepository = mock(SolicitacaoAdocaoRepository.class);
	private final AnimalRepository animalRepository = mock(AnimalRepository.class);
	private final AdotanteRepository adotanteRepository = mock(AdotanteRepository.class);
	private final OperadorRepository operadorRepository = mock(OperadorRepository.class);
	private final SolicitacaoAdocaoService service = new SolicitacaoAdocaoService(
			solicitacaoRepository, animalRepository, adotanteRepository, operadorRepository
	);

	@Test
	void deveCriarSolicitacaoPendente_quandoAdotanteSelecionarAnimalDisponivel() {
		UUID animalId = UUID.randomUUID();
		UUID adotanteId = UUID.randomUUID();
		Animal animal = novoAnimal(UUID.randomUUID());
		Adotante adotante = novoAdotante("Maria", "12345678901");
		when(animalRepository.findById(animalId)).thenReturn(Optional.of(animal));
		when(adotanteRepository.findById(adotanteId)).thenReturn(Optional.of(adotante));
		when(solicitacaoRepository.existsByAnimalIdAndAdotanteIdAndStatus(animalId, adotanteId, SolicitacaoStatus.PENDENTE))
				.thenReturn(false);
		when(solicitacaoRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

		SolicitacaoAdocaoResponse response = service.criar(animalId, adotanteId);

		assertEquals(SolicitacaoStatus.PENDENTE, response.status());
		assertEquals("Luna", response.animalNome());
		verify(solicitacaoRepository).save(any(SolicitacaoAdocao.class));
	}

	@Test
	void deveLancarExcecao_quandoAdotanteJaPossuiSolicitacaoPendente() {
		UUID animalId = UUID.randomUUID();
		UUID adotanteId = UUID.randomUUID();
		when(animalRepository.findById(animalId)).thenReturn(Optional.of(novoAnimal(UUID.randomUUID())));
		when(adotanteRepository.findById(adotanteId)).thenReturn(Optional.of(novoAdotante("Maria", "12345678901")));
		when(solicitacaoRepository.existsByAnimalIdAndAdotanteIdAndStatus(animalId, adotanteId, SolicitacaoStatus.PENDENTE))
				.thenReturn(true);

		assertThrows(IllegalStateException.class, () -> service.criar(animalId, adotanteId));
	}

	@Test
	void deveAprovarERejeitarConcorrentes_quandoOperadorDoLarSelecionarCandidato() {
		UUID larId = UUID.randomUUID();
		UUID operadorId = UUID.randomUUID();
		UUID solicitacaoId = UUID.randomUUID();
		Lar lar = novoLar(larId);
		Operador operador = novoOperador(lar);
		Animal animal = novoAnimal(larId);
		SolicitacaoAdocao selecionada = novaSolicitacao(solicitacaoId, animal, novoAdotante("Maria", "12345678901"));
		SolicitacaoAdocao concorrente = novaSolicitacao(UUID.randomUUID(), animal, novoAdotante("Joao", "98765432109"));
		when(operadorRepository.findById(operadorId)).thenReturn(Optional.of(operador));
		when(solicitacaoRepository.findById(solicitacaoId)).thenReturn(Optional.of(selecionada));
		when(solicitacaoRepository.findByAnimalIdAndStatus(animal.getId(), SolicitacaoStatus.PENDENTE))
				.thenReturn(List.of(selecionada, concorrente));

		service.aprovar(solicitacaoId, operadorId);

		assertEquals(SolicitacaoStatus.APROVADA, selecionada.getStatus());
		assertEquals(SolicitacaoStatus.REJEITADA, concorrente.getStatus());
		assertEquals(AnimalStatus.ADOTADO, animal.getStatus());
		verify(animalRepository).save(animal);
	}

	@Test
	void deveNegarAprovacao_quandoOperadorForDeOutroLar() {
		UUID solicitacaoId = UUID.randomUUID();
		UUID operadorId = UUID.randomUUID();
		Operador operador = novoOperador(novoLar(UUID.randomUUID()));
		SolicitacaoAdocao solicitacao = novaSolicitacao(solicitacaoId, novoAnimal(UUID.randomUUID()), novoAdotante("Maria", "12345678901"));
		when(operadorRepository.findById(operadorId)).thenReturn(Optional.of(operador));
		when(solicitacaoRepository.findById(solicitacaoId)).thenReturn(Optional.of(solicitacao));

		assertThrows(AcessoNegadoException.class, () -> service.aprovar(solicitacaoId, operadorId));
	}

	@Test
	void deveNegarCancelamento_quandoSolicitacaoForDeOutroAdotante() {
		UUID solicitacaoId = UUID.randomUUID();
		UUID adotanteId = UUID.randomUUID();
		Adotante solicitante = novoAdotante("Maria", "12345678901");
		Adotante outroAdotante = novoAdotante("Joao", "98765432109");
		ReflectionTestUtils.setField(solicitante, "id", UUID.randomUUID());
		ReflectionTestUtils.setField(outroAdotante, "id", adotanteId);
		when(adotanteRepository.findById(adotanteId)).thenReturn(Optional.of(outroAdotante));
		when(solicitacaoRepository.findById(solicitacaoId))
				.thenReturn(Optional.of(novaSolicitacao(solicitacaoId, novoAnimal(UUID.randomUUID()), solicitante)));

		assertThrows(AcessoNegadoException.class, () -> service.cancelar(solicitacaoId, adotanteId));
	}

	private Animal novoAnimal(UUID larId) {
		Animal animal = new Animal("Luna", Especie.GATO, Porte.PEQUENO, 2, "Calma", larId);
		ReflectionTestUtils.setField(animal, "id", UUID.randomUUID());
		return animal;
	}

	private Adotante novoAdotante(String nome, String cpf) {
		return new Adotante(nome, "Rua A", nome + "@email.com", "senha", cpf, List.of());
	}

	private Lar novoLar(UUID id) {
		Lar lar = new Lar("Lar Amigo");
		ReflectionTestUtils.setField(lar, "id", id);
		return lar;
	}

	private Operador novoOperador(Lar lar) {
		return new Operador("Responsavel", "Rua Lar", "operador@email.com", "senha", "11122233344", lar);
	}

	private SolicitacaoAdocao novaSolicitacao(UUID id, Animal animal, Adotante adotante) {
		SolicitacaoAdocao solicitacao = new SolicitacaoAdocao(animal, adotante);
		ReflectionTestUtils.setField(solicitacao, "id", id);
		return solicitacao;
	}
}
