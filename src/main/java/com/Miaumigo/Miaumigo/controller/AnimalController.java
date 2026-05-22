package com.Miaumigo.Miaumigo.controller;

import com.Miaumigo.Miaumigo.dto.AcaoRealizadaResponse;
import com.Miaumigo.Miaumigo.dto.AnimalResponse;
import com.Miaumigo.Miaumigo.dto.CadastroAnimalRequest;
import com.Miaumigo.Miaumigo.dto.RealizarAdocaoRequest;
import com.Miaumigo.Miaumigo.service.AnimalService;
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

	public AnimalController(AnimalService animalService) {
		this.animalService = animalService;
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
}
