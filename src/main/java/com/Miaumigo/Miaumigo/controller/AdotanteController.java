package com.Miaumigo.Miaumigo.controller;

import com.Miaumigo.Miaumigo.dto.AdotanteResponse;
import com.Miaumigo.Miaumigo.dto.AnimalRecomendadoResponse;
import com.Miaumigo.Miaumigo.dto.CadastroAdotanteRequest;
import com.Miaumigo.Miaumigo.dto.SolicitacaoAdocaoResponse;
import com.Miaumigo.Miaumigo.service.AdotanteService;
import com.Miaumigo.Miaumigo.service.MatchmakingService;
import com.Miaumigo.Miaumigo.service.SolicitacaoAdocaoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/adotantes")
public class AdotanteController {
	//Coment
	private final AdotanteService adotanteService;
	private final MatchmakingService matchmakingService;
	private final SolicitacaoAdocaoService solicitacaoService;

	public AdotanteController(
			AdotanteService adotanteService,
			MatchmakingService matchmakingService,
			SolicitacaoAdocaoService solicitacaoService
	) {
		this.adotanteService = adotanteService;
		this.matchmakingService = matchmakingService;
		this.solicitacaoService = solicitacaoService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public AdotanteResponse cadastrar(@Valid @RequestBody CadastroAdotanteRequest request) {
		return adotanteService.cadastrar(request);
	}

	@GetMapping("/me/animais-recomendados")
	public List<AnimalRecomendadoResponse> recomendarAnimais(@RequestHeader("X-Usuario-Id") UUID usuarioId) {
		return matchmakingService.recomendarAnimais(usuarioId);
	}

	@GetMapping("/me/solicitacoes")
	public List<SolicitacaoAdocaoResponse> listarSolicitacoes(@RequestHeader("X-Usuario-Id") UUID usuarioId) {
		return solicitacaoService.listarDoAdotante(usuarioId);
	}
}
