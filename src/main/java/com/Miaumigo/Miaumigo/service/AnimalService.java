package com.Miaumigo.Miaumigo.service;

import com.Miaumigo.Miaumigo.domain.Animal;
import com.Miaumigo.Miaumigo.dto.AcaoRealizadaResponse;
import com.Miaumigo.Miaumigo.dto.AnimalResponse;
import com.Miaumigo.Miaumigo.dto.CadastroAnimalRequest;
import com.Miaumigo.Miaumigo.dto.RealizarAdocaoRequest;
import com.Miaumigo.Miaumigo.exception.RecursoNaoEncontradoException;
import com.Miaumigo.Miaumigo.repository.AnimalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AnimalService {

	private final AnimalRepository animalRepository;

	public AnimalService(AnimalRepository animalRepository) {
		this.animalRepository = animalRepository;
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

	//Refatorar quando entidade usuario existir
	public AcaoRealizadaResponse realizarAdocao(UUID id, RealizarAdocaoRequest request) {
		Animal animal = animalRepository.findById(id)
				.orElseThrow(() -> new RecursoNaoEncontradoException("Animal não encontrado."));

		animal.realizarAdocao(request.adotadoPor());
		animalRepository.save(animal);

		return new AcaoRealizadaResponse("Adoção realizada com sucesso.");
	}
}
