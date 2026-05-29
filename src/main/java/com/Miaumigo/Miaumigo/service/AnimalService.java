package com.Miaumigo.Miaumigo.service;

import com.Miaumigo.Miaumigo.domain.Adotante;
import com.Miaumigo.Miaumigo.domain.Animal;
import com.Miaumigo.Miaumigo.domain.AnimalStatus;
import com.Miaumigo.Miaumigo.domain.Operador;
import com.Miaumigo.Miaumigo.dto.AcaoRealizadaResponse;
import com.Miaumigo.Miaumigo.dto.AnimalResponse;
import com.Miaumigo.Miaumigo.dto.CadastroAnimalRequest;
import com.Miaumigo.Miaumigo.exception.IdentidadeNaoAutenticadaException;
import com.Miaumigo.Miaumigo.exception.RecursoNaoEncontradoException;
import com.Miaumigo.Miaumigo.repository.AdotanteRepository;
import com.Miaumigo.Miaumigo.repository.AnimalRepository;
import com.Miaumigo.Miaumigo.repository.OperadorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class AnimalService {

	private final AnimalRepository animalRepository;
	private final AdotanteRepository adotanteRepository;
	private final OperadorRepository operadorRepository;

	public AnimalService(
			AnimalRepository animalRepository,
			AdotanteRepository adotanteRepository,
			OperadorRepository operadorRepository
	) {
		this.animalRepository = animalRepository;
		this.adotanteRepository = adotanteRepository;
		this.operadorRepository = operadorRepository;
	}

	public void cadastrar(CadastroAnimalRequest request, UUID operadorId) {
		Operador operador = operadorRepository.findById(operadorId)
				.orElseThrow(() -> new IdentidadeNaoAutenticadaException("Operador não autenticado."));
		Animal animal = new Animal(
				request.nome(),
				request.especie(),
				request.porte(),
				request.idade(),
				request.descricao(),
				operador.getLar().getId(),
				request.tags(),
				request.cloudinaryPublicId()
		);

		animalRepository.save(animal);
	}

	@Transactional(readOnly = true)
	public AnimalResponse buscarPorId(UUID id) {
		Animal animal = animalRepository.findById(id)
				.orElseThrow(() -> new RecursoNaoEncontradoException("Animal não encontrado"));

		return toResponse(animal);
	}

	@Transactional(readOnly = true)
	public List<AnimalResponse> listarDisponiveis() {
		return animalRepository.findByStatus(AnimalStatus.DISPONIVEL).stream()
				.map(this::toResponse)
				.toList();
	}

	@Transactional
	public AcaoRealizadaResponse devolverAnimal(UUID id, String motivo) {
		Animal animal = animalRepository.findById(id)
				.orElseThrow(() -> new RecursoNaoEncontradoException("Animal não encontrado."));

		Adotante adotante = animal.devolver(motivo);
		adotante.adicionarLog("Devolveu " + animal.getNome() + ".");
		animalRepository.save(animal);

		return new AcaoRealizadaResponse("Devolução registrada com sucesso.");
	}

	private AnimalResponse toResponse(Animal animal) {
		return new AnimalResponse(
				animal.getId(),
				animal.getNome(),
				animal.getIdade(),
				animal.getPorte(),
				animal.getEspecie(),
				animal.getDescricao(),
				animal.getStatus(),
				animal.getTags(),
				animal.getCloudinaryPublicId()
		);
	}
}
