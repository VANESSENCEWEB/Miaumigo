package com.Miaumigo.Miaumigo.service;

import com.Miaumigo.Miaumigo.domain.Adotante;
import com.Miaumigo.Miaumigo.domain.Animal;
import com.Miaumigo.Miaumigo.domain.AnimalStatus;
import com.Miaumigo.Miaumigo.domain.ExperienciaAnimais;
import com.Miaumigo.Miaumigo.domain.Porte;
import com.Miaumigo.Miaumigo.domain.Tag;
import com.Miaumigo.Miaumigo.domain.TempoDisponivel;
import com.Miaumigo.Miaumigo.domain.TipoMoradia;
import com.Miaumigo.Miaumigo.dto.AnimalRecomendadoResponse;
import com.Miaumigo.Miaumigo.exception.IdentidadeNaoAutenticadaException;
import com.Miaumigo.Miaumigo.repository.AdotanteRepository;
import com.Miaumigo.Miaumigo.repository.AnimalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
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
				.orElseThrow(() -> new IdentidadeNaoAutenticadaException("Adotante não autenticado."));
		List<Animal> animaisDisponiveis = removerDuplicados(animalRepository.findByStatus(AnimalStatus.DISPONIVEL));
		List<AnimalRecomendadoResponse> recomendacoes = new ArrayList<>();

		for (Animal animal : animaisDisponiveis) {
			int compatibilidade = calcularCompatibilidade(adotante, animal);
			recomendacoes.add(toResponse(animal, compatibilidade));
		}

		recomendacoes.sort(Comparator.comparingInt(AnimalRecomendadoResponse::compatibilidade).reversed());
		return recomendacoes;
	}

	private List<Animal> removerDuplicados(List<Animal> animais) {
		Set<String> chavesEncontradas = new LinkedHashSet<>();
		List<Animal> animaisUnicos = new ArrayList<>();
		for (Animal animal : animais) {
			if (chavesEncontradas.add(chaveAnimal(animal))) {
				animaisUnicos.add(animal);
			}
		}
		return animaisUnicos;
	}

	private String chaveAnimal(Animal animal) {
		if (animal.getCloudinaryPublicId() != null) {
			return "imagem:" + animal.getCloudinaryPublicId().trim().toLowerCase(Locale.ROOT);
		}
		return "%s|%s|%s|%s".formatted(
				normalizarChave(animal.getNome()),
				animal.getEspecie(),
				animal.getPorte(),
				animal.getIdade()
		);
	}

	private String normalizarChave(String valor) {
		return valor == null ? "" : valor.trim().toLowerCase(Locale.ROOT);
	}

	private int calcularCompatibilidade(Adotante adotante, Animal animal) {
		int pontos = 0;
		pontos += pontuarEspecie(adotante, animal);
		pontos += pontuarTags(adotante.getPreferencias(), animal.getTags());
		pontos += pontuarPorte(adotante.getEspacoDisponivel(), animal.getPorte());
		pontos += pontuarMoradia(adotante, animal.getTags());
		pontos += pontuarTempo(adotante.getTempoDisponivel(), animal.getTags());
		pontos += pontuarExperiencia(adotante.getExperienciaAnimais(), animal.getTags());
		pontos += pontuarContexto(adotante, animal.getTags());
		return Math.max(0, Math.min(100, pontos));
	}

	private int pontuarEspecie(Adotante adotante, Animal animal) {
		if (adotante.getEspeciesPreferidas().isEmpty()) {
			return 12;
		}
		return adotante.getEspeciesPreferidas().contains(animal.getEspecie()) ? 25 : 0;
	}

	private int pontuarTags(List<Tag> preferencias, List<Tag> tags) {
		if (preferencias.isEmpty()) {
			return 12;
		}
		long matches = preferencias.stream()
				.filter(tags::contains)
				.count();
		return (int) Math.round((matches * 25.0) / preferencias.size());
	}

	private int pontuarPorte(Porte espacoDisponivel, Porte porteAnimal) {
		if (espacoDisponivel == null) {
			return 7;
		}
		if (porteAnimal == Porte.PEQUENO) {
			return 15;
		}
		if (porteAnimal == Porte.MEDIO) {
			return espacoDisponivel == Porte.PEQUENO ? 7 : 15;
		}
		return espacoDisponivel == Porte.GRANDE ? 15 : 0;
	}

	private int pontuarMoradia(Adotante adotante, List<Tag> tags) {
		TipoMoradia tipoMoradia = adotante.getTipoMoradia();
		Porte espaco = adotante.getEspacoDisponivel();
		if (tags.contains(Tag.PRECISA_DE_ESPACO)) {
			boolean possuiEspaco = espaco == Porte.GRANDE
					|| tipoMoradia == TipoMoradia.CASA_COM_QUINTAL
					|| tipoMoradia == TipoMoradia.SITIO_CHACARA;
			return possuiEspaco ? 10 : 0;
		}
		if (tags.contains(Tag.ADAPTADO_A_APARTAMENTO)) {
			return tipoMoradia == TipoMoradia.APARTAMENTO || espaco == Porte.PEQUENO ? 10 : 6;
		}
		return 7;
	}

	private int pontuarTempo(TempoDisponivel tempoDisponivel, List<Tag> tags) {
		if (!tags.contains(Tag.ENERGICO)) {
			return 10;
		}
		if (tempoDisponivel == TempoDisponivel.DUAS_HORAS_OU_MAIS) {
			return 10;
		}
		return tempoDisponivel == TempoDisponivel.UMA_HORA ? 5 : 0;
	}

	private int pontuarExperiencia(ExperienciaAnimais experiencia, List<Tag> tags) {
		if ((tags.contains(Tag.PROTETOR) || tags.contains(Tag.ENERGICO))
				&& experiencia == ExperienciaAnimais.PRIMEIRA_ADOCAO) {
			return 0;
		}
		return experiencia == null ? 5 : 8;
	}

	private int pontuarContexto(Adotante adotante, List<Tag> tags) {
		int pontos = 5;
		if (Boolean.TRUE.equals(adotante.getPossuiCriancas()) && tags.contains(Tag.CONVIVE_COM_CRIANCAS)) {
			pontos += 2;
		}
		if (Boolean.TRUE.equals(adotante.getPossuiCaes()) && tags.contains(Tag.CONVIVE_COM_CAES)) {
			pontos += 2;
		}
		if (Boolean.TRUE.equals(adotante.getPossuiGatos()) && tags.contains(Tag.CONVIVE_COM_GATOS)) {
			pontos += 1;
		}
		return Math.min(7, pontos);
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
