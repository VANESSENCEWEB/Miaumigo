package com.Miaumigo.Miaumigo.service;

import com.Miaumigo.Miaumigo.domain.Adotante;
import com.Miaumigo.Miaumigo.domain.Animal;
import com.Miaumigo.Miaumigo.domain.AnimalStatus;
import com.Miaumigo.Miaumigo.domain.Operador;
import com.Miaumigo.Miaumigo.domain.SolicitacaoAdocao;
import com.Miaumigo.Miaumigo.domain.SolicitacaoStatus;
import com.Miaumigo.Miaumigo.dto.SolicitacaoAdocaoResponse;
import com.Miaumigo.Miaumigo.exception.AcessoNegadoException;
import com.Miaumigo.Miaumigo.exception.IdentidadeNaoAutenticadaException;
import com.Miaumigo.Miaumigo.exception.RecursoNaoEncontradoException;
import com.Miaumigo.Miaumigo.repository.AdotanteRepository;
import com.Miaumigo.Miaumigo.repository.AnimalRepository;
import com.Miaumigo.Miaumigo.repository.OperadorRepository;
import com.Miaumigo.Miaumigo.repository.SolicitacaoAdocaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class SolicitacaoAdocaoService {

	private final SolicitacaoAdocaoRepository solicitacaoRepository;
	private final AnimalRepository animalRepository;
	private final AdotanteRepository adotanteRepository;
	private final OperadorRepository operadorRepository;

	public SolicitacaoAdocaoService(
			SolicitacaoAdocaoRepository solicitacaoRepository,
			AnimalRepository animalRepository,
			AdotanteRepository adotanteRepository,
			OperadorRepository operadorRepository
	) {
		this.solicitacaoRepository = solicitacaoRepository;
		this.animalRepository = animalRepository;
		this.adotanteRepository = adotanteRepository;
		this.operadorRepository = operadorRepository;
	}

	@Transactional
	public SolicitacaoAdocaoResponse criar(UUID animalId, UUID adotanteId) {
		Animal animal = buscarAnimal(animalId);
		Adotante adotante = buscarAdotante(adotanteId);
		if (animal.getStatus() != AnimalStatus.DISPONIVEL) {
			throw new IllegalStateException("Apenas animais disponíveis aceitam solicitações.");
		}
		if (solicitacaoRepository.existsByAnimalIdAndAdotanteIdAndStatus(
				animalId, adotanteId, SolicitacaoStatus.PENDENTE)) {
			throw new IllegalStateException("Adotante já possui solicitação pendente para este animal.");
		}
		return toResponse(solicitacaoRepository.save(new SolicitacaoAdocao(animal, adotante)));
	}

	@Transactional(readOnly = true)
	public List<SolicitacaoAdocaoResponse> listarDoAdotante(UUID adotanteId) {
		buscarAdotante(adotanteId);
		return solicitacaoRepository.findByAdotanteIdOrderByCriadoEmDesc(adotanteId).stream()
				.map(this::toResponse)
				.toList();
	}

	@Transactional
	public SolicitacaoAdocaoResponse cancelar(UUID solicitacaoId, UUID adotanteId) {
		Adotante adotante = buscarAdotante(adotanteId);
		SolicitacaoAdocao solicitacao = buscarSolicitacao(solicitacaoId);
		if (!Objects.equals(solicitacao.getAdotante().getId(), adotanteId)) {
			throw new AcessoNegadoException("Solicitação pertence a outro adotante.");
		}
		solicitacao.cancelar(adotante);
		return toResponse(solicitacaoRepository.save(solicitacao));
	}

	@Transactional(readOnly = true)
	public List<SolicitacaoAdocaoResponse> listarDoLar(UUID operadorId, SolicitacaoStatus status) {
		Operador operador = buscarOperador(operadorId);
		SolicitacaoStatus filtro = status == null ? SolicitacaoStatus.PENDENTE : status;
		return solicitacaoRepository.findByAnimalLarIdAndStatus(operador.getLar().getId(), filtro).stream()
				.map(this::toResponse)
				.toList();
	}

	@Transactional
	public SolicitacaoAdocaoResponse aprovar(UUID solicitacaoId, UUID operadorId) {
		Operador operador = buscarOperador(operadorId);
		SolicitacaoAdocao selecionada = buscarSolicitacao(solicitacaoId);
		validarOperadorDoLar(operador, selecionada.getAnimal());
		selecionada.aprovar(operador);
		selecionada.getAnimal().realizarAdocao(selecionada.getAdotante());
		selecionada.getAdotante().adicionarLog("Adotou " + selecionada.getAnimal().getNome() + ".");

		List<SolicitacaoAdocao> pendentes = solicitacaoRepository.findByAnimalIdAndStatus(
				selecionada.getAnimal().getId(), SolicitacaoStatus.PENDENTE);
		pendentes.stream()
				.filter(solicitacao -> solicitacao != selecionada
						&& !solicitacao.getId().equals(selecionada.getId()))
				.forEach(solicitacao -> solicitacao.rejeitar(operador));
		solicitacaoRepository.saveAll(pendentes);
		solicitacaoRepository.save(selecionada);
		animalRepository.save(selecionada.getAnimal());
		return toResponse(selecionada);
	}

	@Transactional
	public SolicitacaoAdocaoResponse rejeitar(UUID solicitacaoId, UUID operadorId) {
		Operador operador = buscarOperador(operadorId);
		SolicitacaoAdocao solicitacao = buscarSolicitacao(solicitacaoId);
		validarOperadorDoLar(operador, solicitacao.getAnimal());
		solicitacao.rejeitar(operador);
		return toResponse(solicitacaoRepository.save(solicitacao));
	}

	private void validarOperadorDoLar(Operador operador, Animal animal) {
		if (!operador.getLar().getId().equals(animal.getLarId())) {
			throw new AcessoNegadoException("Operador não pertence ao lar responsável pelo animal.");
		}
	}

	private Animal buscarAnimal(UUID id) {
		return animalRepository.findById(id)
				.orElseThrow(() -> new RecursoNaoEncontradoException("Animal não encontrado."));
	}

	private Adotante buscarAdotante(UUID id) {
		return adotanteRepository.findById(id)
				.orElseThrow(() -> new IdentidadeNaoAutenticadaException("Adotante não autenticado."));
	}

	private Operador buscarOperador(UUID id) {
		return operadorRepository.findById(id)
				.orElseThrow(() -> new IdentidadeNaoAutenticadaException("Operador não autenticado."));
	}

	private SolicitacaoAdocao buscarSolicitacao(UUID id) {
		return solicitacaoRepository.findById(id)
				.orElseThrow(() -> new RecursoNaoEncontradoException("Solicitação não encontrada."));
	}

	private SolicitacaoAdocaoResponse toResponse(SolicitacaoAdocao solicitacao) {
		return new SolicitacaoAdocaoResponse(
				solicitacao.getId(),
				solicitacao.getAnimal().getId(),
				solicitacao.getAnimal().getNome(),
				solicitacao.getAdotante().getId(),
				solicitacao.getAdotante().getNome(),
				solicitacao.getStatus(),
				solicitacao.getCriadoEm(),
				solicitacao.getAtualizadoEm()
		);
	}
}
