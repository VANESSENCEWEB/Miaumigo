package com.Miaumigo.Miaumigo.service;

import com.Miaumigo.Miaumigo.domain.Animal;
import com.Miaumigo.Miaumigo.dto.CadastroAnimalRequest;
import com.Miaumigo.Miaumigo.repository.AnimalRepository;
import org.springframework.stereotype.Service;

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
}
