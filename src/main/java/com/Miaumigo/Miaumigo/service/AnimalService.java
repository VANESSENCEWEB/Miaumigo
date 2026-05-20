package com.Miaumigo.Miaumigo.service;

import com.Miaumigo.Miaumigo.domain.Animal;
import com.Miaumigo.Miaumigo.dto.AnimalResponse;
import com.Miaumigo.Miaumigo.dto.AtualizacaoAnimalRequest;
import com.Miaumigo.Miaumigo.dto.CadastroAnimalRequest;
import com.Miaumigo.Miaumigo.exception.RecursoNaoEncontradoException;
import com.Miaumigo.Miaumigo.repository.AnimalRepository;
import org.springframework.stereotype.Service;

import java.util.List;
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
				request.larId()
		);

		animalRepository.save(animal);
	}

	public List<AnimalResponse> listarTodos() {
		return animalRepository.findAll()
				.stream()
				.map(AnimalResponse::from)
				.toList();
	}

	public void atualizar(UUID id, AtualizacaoAnimalRequest request) {
		Animal animal = animalRepository.findById(id)
				.orElseThrow(() -> new RecursoNaoEncontradoException("Animal não encontrado."));

		animal.atualizarDados(
				request.nome(),
				request.especie(),
				request.porte(),
				request.idade(),
				request.descricao()
		);

		animalRepository.save(animal);
	}
}
