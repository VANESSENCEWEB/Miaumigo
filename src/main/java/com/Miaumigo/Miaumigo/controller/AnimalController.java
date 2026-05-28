package com.Miaumigo.Miaumigo.controller;

import com.Miaumigo.Miaumigo.dto.AnimalResponse;
import com.Miaumigo.Miaumigo.dto.CadastroAnimalRequest;
import com.Miaumigo.Miaumigo.dto.SolicitacaoAdocaoResponse;
import com.Miaumigo.Miaumigo.dto.TextoDivulgacaoResponse;
import com.Miaumigo.Miaumigo.security.UsuarioAutenticadoService;
import com.Miaumigo.Miaumigo.service.AnimalService;
import com.Miaumigo.Miaumigo.service.SolicitacaoAdocaoService;
import com.Miaumigo.Miaumigo.service.TextoDivulgacaoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/animais")
public class AnimalController {

	private final AnimalService animalService;
	private final SolicitacaoAdocaoService solicitacaoService;
	private final TextoDivulgacaoService textoDivulgacaoService;
	private final UsuarioAutenticadoService usuarioAutenticadoService;

	public AnimalController(
			AnimalService animalService,
			SolicitacaoAdocaoService solicitacaoService,
			TextoDivulgacaoService textoDivulgacaoService,
			UsuarioAutenticadoService usuarioAutenticadoService
	) {
		this.animalService = animalService;
		this.solicitacaoService = solicitacaoService;
		this.textoDivulgacaoService = textoDivulgacaoService;
		this.usuarioAutenticadoService = usuarioAutenticadoService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public void cadastrar(
			@Valid @RequestBody CadastroAnimalRequest request,
			@AuthenticationPrincipal Jwt jwt
	) {
		animalService.cadastrar(request, usuarioAutenticadoService.exigirOperador(jwt));
	}

	@GetMapping("/{id}")
	public AnimalResponse buscarPorId(@PathVariable UUID id) {
		return animalService.buscarPorId(id);
	}

	@PostMapping("/{id}/solicitacoes")
	@ResponseStatus(HttpStatus.CREATED)
	public SolicitacaoAdocaoResponse solicitarAdocao(
			@PathVariable UUID id,
			@AuthenticationPrincipal Jwt jwt
		) {
		return solicitacaoService.criar(id, usuarioAutenticadoService.exigirAdotante(jwt));
	}

	@PostMapping("/{id}/texto-divulgacao")
	public TextoDivulgacaoResponse gerarTextoDivulgacao(
			@PathVariable UUID id,
			@AuthenticationPrincipal Jwt jwt
	) {
		usuarioAutenticadoService.exigirOperadorOuAdmin(jwt);
		return textoDivulgacaoService.gerar(id);
	}
}
