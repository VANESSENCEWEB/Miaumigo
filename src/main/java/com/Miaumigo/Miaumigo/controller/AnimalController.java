package com.Miaumigo.Miaumigo.controller;

import com.Miaumigo.Miaumigo.dto.AnimalResponse;
import com.Miaumigo.Miaumigo.dto.CadastroAnimalRequest;
import com.Miaumigo.Miaumigo.dto.SolicitacaoAdocaoResponse;
import com.Miaumigo.Miaumigo.dto.TextoDivulgacaoResponse;
import com.Miaumigo.Miaumigo.service.AnimalService;
import com.Miaumigo.Miaumigo.service.SolicitacaoAdocaoService;
import com.Miaumigo.Miaumigo.service.TextoDivulgacaoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/animais")
public class AnimalController {

	private final AnimalService animalService;
	private final SolicitacaoAdocaoService solicitacaoService;
	private final TextoDivulgacaoService textoDivulgacaoService;

	public AnimalController(
			AnimalService animalService,
			SolicitacaoAdocaoService solicitacaoService,
			TextoDivulgacaoService textoDivulgacaoService
	) {
		this.animalService = animalService;
		this.solicitacaoService = solicitacaoService;
		this.textoDivulgacaoService = textoDivulgacaoService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public void cadastrar(
			@Valid @RequestBody CadastroAnimalRequest request,
			@RequestHeader("X-Usuario-Id") UUID usuarioId
	) {
		animalService.cadastrar(request, usuarioId);
	}

	@GetMapping("/{id}")
	public AnimalResponse buscarPorId(@PathVariable UUID id) {
		return animalService.buscarPorId(id);
	}

	@PostMapping("/{id}/solicitacoes")
	@ResponseStatus(HttpStatus.CREATED)
	public SolicitacaoAdocaoResponse solicitarAdocao(
			@PathVariable UUID id,
			@RequestHeader("X-Usuario-Id") UUID usuarioId
		) {
		return solicitacaoService.criar(id, usuarioId);
	}

	@PostMapping("/{id}/texto-divulgacao")
	public TextoDivulgacaoResponse gerarTextoDivulgacao(@PathVariable UUID id) {
		return textoDivulgacaoService.gerar(id);
	}
}
