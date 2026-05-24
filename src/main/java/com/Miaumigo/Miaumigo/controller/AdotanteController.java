package com.Miaumigo.Miaumigo.controller;

import com.Miaumigo.Miaumigo.dto.AdotanteResponse;
import com.Miaumigo.Miaumigo.dto.CadastroAdotanteRequest;
import com.Miaumigo.Miaumigo.service.AdotanteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/adotantes")
public class AdotanteController {

	private final AdotanteService adotanteService;

	public AdotanteController(AdotanteService adotanteService) {
		this.adotanteService = adotanteService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public AdotanteResponse cadastrar(@Valid @RequestBody CadastroAdotanteRequest request) {
		return adotanteService.cadastrar(request);
	}
}
