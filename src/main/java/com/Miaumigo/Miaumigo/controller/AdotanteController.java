package com.Miaumigo.Miaumigo.controller;

import com.Miaumigo.Miaumigo.dto.AdotanteResponse;
import com.Miaumigo.Miaumigo.dto.AnimalRecomendadoResponse;
import com.Miaumigo.Miaumigo.dto.CadastroAdotanteRequest;
import com.Miaumigo.Miaumigo.dto.SolicitacaoAdocaoResponse;
import com.Miaumigo.Miaumigo.security.UsuarioAutenticadoService;
import com.Miaumigo.Miaumigo.service.AdotanteService;
import com.Miaumigo.Miaumigo.service.MatchmakingService;
import com.Miaumigo.Miaumigo.service.SolicitacaoAdocaoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
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
	//Coment
	private final AdotanteService adotanteService;
	private final MatchmakingService matchmakingService;
	private final SolicitacaoAdocaoService solicitacaoService;
	private final UsuarioAutenticadoService usuarioAutenticadoService;

	public AdotanteController(
			AdotanteService adotanteService,
			MatchmakingService matchmakingService,
			SolicitacaoAdocaoService solicitacaoService,
			UsuarioAutenticadoService usuarioAutenticadoService
	) {
		this.adotanteService = adotanteService;
		this.matchmakingService = matchmakingService;
		this.solicitacaoService = solicitacaoService;
		this.usuarioAutenticadoService = usuarioAutenticadoService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public AdotanteResponse cadastrar(@Valid @RequestBody CadastroAdotanteRequest request) {
		return adotanteService.cadastrar(request);
	}

	@GetMapping("/me/animais-recomendados")
	public List<AnimalRecomendadoResponse> recomendarAnimais(@AuthenticationPrincipal Jwt jwt) {
		return matchmakingService.recomendarAnimais(usuarioAutenticadoService.exigirAdotante(jwt));
	}

	@GetMapping("/me/solicitacoes")
	public List<SolicitacaoAdocaoResponse> listarSolicitacoes(@AuthenticationPrincipal Jwt jwt) {
		return solicitacaoService.listarDoAdotante(usuarioAutenticadoService.exigirAdotante(jwt));
	}
}
