package com.Miaumigo.Miaumigo.controller;

import com.Miaumigo.Miaumigo.dto.AdotanteResponse;
import com.Miaumigo.Miaumigo.dto.AnimalRecomendadoResponse;
import com.Miaumigo.Miaumigo.dto.CadastroAdotanteRequest;
import com.Miaumigo.Miaumigo.service.AdotanteService;
import com.Miaumigo.Miaumigo.service.MatchmakingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/adotantes")
public class AdotanteController {

	private final AdotanteService adotanteService;
	private final MatchmakingService matchmakingService;

	public AdotanteController(AdotanteService adotanteService, MatchmakingService matchmakingService) {
		this.adotanteService = adotanteService;
		this.matchmakingService = matchmakingService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public AdotanteResponse cadastrar(@Valid @RequestBody CadastroAdotanteRequest request) {
		return adotanteService.cadastrar(request);
	}

	@GetMapping("/{id}/animais-recomendados")
	public List<AnimalRecomendadoResponse> recomendarAnimais(@PathVariable UUID id) {
		return matchmakingService.recomendarAnimais(id);
	}
}
