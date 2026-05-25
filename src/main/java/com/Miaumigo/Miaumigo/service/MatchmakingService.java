package com.Miaumigo.Miaumigo.service;

import com.Miaumigo.Miaumigo.domain.Adotante;
import com.Miaumigo.Miaumigo.domain.Animal;
import com.Miaumigo.Miaumigo.domain.AnimalStatus;
import com.Miaumigo.Miaumigo.domain.Tag;
import com.Miaumigo.Miaumigo.dto.AnimalRecomendadoResponse;
import com.Miaumigo.Miaumigo.exception.RecursoNaoEncontradoException;
import com.Miaumigo.Miaumigo.repository.AdotanteRepository;
import com.Miaumigo.Miaumigo.repository.AnimalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class MatchmakingService {

	private final AnimalRepository animalRepository;
	private final AdotanteRepository adotanteRepository;

	public MatchmakingService(AnimalRepository animalRepository, AdotanteRepository adotanteRepository) {
		this.animalRepository = animalRepository;
		this.adotanteRepository = adotanteRepository;
	}

	@Transactional(readOnly = true)
	public List<AnimalRecomendadoResponse> recomendarAnimais(UUID adotanteId) {
		Adotante adotante = adotanteRepository.findById(adotanteId)
				.orElseThrow(() -> new RecursoNaoEncontradoException("Adotante não encontrado."));
		List<Animal> animaisDisponiveis = animalRepository.findByStatus(AnimalStatus.DISPONIVEL);
		List<AnimalRecomendadoResponse> recomendacoes = new ArrayList<>();

		for (Animal animal : animaisDisponiveis) {
			int compatibilidade = calcularCompatibilidade(adotante.getPreferencias(), animal.getTags());
			recomendacoes.add(toResponse(animal, compatibilidade));
		}

		recomendacoes.sort(Comparator.comparingInt(AnimalRecomendadoResponse::compatibilidade).reversed());
		return recomendacoes;
	}

	private int calcularCompatibilidade(List<Tag> preferencias, List<Tag> tags) {
		int compatibilidade = 0;
		for (Tag preferencia : preferencias) {
			if (tags.contains(preferencia)) {
				compatibilidade++;
			}
		}
		return compatibilidade;
	}

	private AnimalRecomendadoResponse toResponse(Animal animal, int compatibilidade) {
		return new AnimalRecomendadoResponse(
				animal.getId(),
				animal.getNome(),
				animal.getIdade(),
				animal.getPorte(),
				animal.getEspecie(),
				animal.getTags(),
				animal.getCloudinaryPublicId(),
				compatibilidade
		);
	}
}
