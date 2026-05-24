package com.Miaumigo.Miaumigo.service;

import com.Miaumigo.Miaumigo.domain.Adotante;
import com.Miaumigo.Miaumigo.domain.Animal;
import com.Miaumigo.Miaumigo.dto.AcaoRealizadaResponse;
import com.Miaumigo.Miaumigo.dto.AnimalResponse;
import com.Miaumigo.Miaumigo.dto.CadastroAnimalRequest;
import com.Miaumigo.Miaumigo.dto.RealizarAdocaoRequest;
import com.Miaumigo.Miaumigo.exception.RecursoNaoEncontradoException;
import com.Miaumigo.Miaumigo.repository.AdotanteRepository;
import com.Miaumigo.Miaumigo.repository.AnimalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AnimalService {

	private final AnimalRepository animalRepository;
	private final AdotanteRepository adotanteRepository;

	public AnimalService(AnimalRepository animalRepository, AdotanteRepository adotanteRepository) {
		this.animalRepository = animalRepository;
		this.adotanteRepository = adotanteRepository;
	}

	public void cadastrar(CadastroAnimalRequest request) {
		Animal animal = new Animal(
				request.nome(),
				request.especie(),
				request.porte(),
				request.idade(),
				request.descricao(),
				request.larId(),
				request.tags(),
				request.cloudinaryPublicId()
		);

		animalRepository.save(animal);
	}

	@Transactional(readOnly = true)
	public AnimalResponse buscarPorId(UUID id) {
		Animal animal = animalRepository.findById(id)
				.orElseThrow(() -> new RecursoNaoEncontradoException("Animal não encontrado"));

		return new AnimalResponse(
				animal.getId(),
				animal.getNome(),
				animal.getIdade(),
				animal.getPorte(),
				animal.getEspecie(),
				animal.getTags(),
				animal.getCloudinaryPublicId()
		);
	}

	@Transactional
	public AcaoRealizadaResponse realizarAdocao(UUID id, RealizarAdocaoRequest request) {
		Animal animal = animalRepository.findById(id)
				.orElseThrow(() -> new RecursoNaoEncontradoException("Animal não encontrado."));
		Adotante adotante = adotanteRepository.findById(request.adotanteId())
				.orElseThrow(() -> new RecursoNaoEncontradoException("Adotante não encontrado."));

		animal.realizarAdocao(adotante);
		adotante.adicionarLog("Adotou " + animal.getNome() + ".");
		animalRepository.save(animal);

		return new AcaoRealizadaResponse("Adoção realizada com sucesso.");
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
}
