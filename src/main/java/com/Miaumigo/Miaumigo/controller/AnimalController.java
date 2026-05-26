package com.Miaumigo.Miaumigo.controller;

import com.Miaumigo.Miaumigo.dto.AcaoRealizadaResponse;
import com.Miaumigo.Miaumigo.dto.AnimalResponse;
import com.Miaumigo.Miaumigo.dto.CadastroAnimalRequest;
import com.Miaumigo.Miaumigo.dto.RealizarAdocaoRequest;
import com.Miaumigo.Miaumigo.dto.TextoDivulgacaoResponse;
import com.Miaumigo.Miaumigo.service.AnimalService;
import com.Miaumigo.Miaumigo.service.TextoDivulgacaoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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
	private final TextoDivulgacaoService textoDivulgacaoService;

	public AnimalController(AnimalService animalService, TextoDivulgacaoService textoDivulgacaoService) {
		this.animalService = animalService;
		this.textoDivulgacaoService = textoDivulgacaoService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public void cadastrar(@Valid @RequestBody CadastroAnimalRequest request) {
		animalService.cadastrar(request);
	}

	@GetMapping("/{id}")
	public AnimalResponse buscarPorId(@PathVariable UUID id) {
		return animalService.buscarPorId(id);
	}

	@PostMapping("/{id}/adocao")
	public AcaoRealizadaResponse realizarAdocao(
			@PathVariable UUID id,
			@Valid @RequestBody RealizarAdocaoRequest request
	) {
		return animalService.realizarAdocao(id, request);
	}

	@PostMapping("/{id}/texto-divulgacao")
	public TextoDivulgacaoResponse gerarTextoDivulgacao(@PathVariable UUID id) {
		return textoDivulgacaoService.gerar(id);
	}
}
