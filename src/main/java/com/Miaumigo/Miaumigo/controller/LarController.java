package com.Miaumigo.Miaumigo.controller;

import com.Miaumigo.Miaumigo.domain.SolicitacaoStatus;
import com.Miaumigo.Miaumigo.dto.CadastroLarRequest;
import com.Miaumigo.Miaumigo.dto.CadastroOperadorRequest;
import com.Miaumigo.Miaumigo.dto.LarResponse;
import com.Miaumigo.Miaumigo.dto.OperadorResponse;
import com.Miaumigo.Miaumigo.dto.SolicitacaoAdocaoResponse;
import com.Miaumigo.Miaumigo.service.LarService;
import com.Miaumigo.Miaumigo.service.SolicitacaoAdocaoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/lares")
public class LarController {

	private final LarService larService;
	private final SolicitacaoAdocaoService solicitacaoService;

	public LarController(LarService larService, SolicitacaoAdocaoService solicitacaoService) {
		this.larService = larService;
		this.solicitacaoService = solicitacaoService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public LarResponse cadastrar(@Valid @RequestBody CadastroLarRequest request) {
		return larService.cadastrar(request);
	}

	@PostMapping("/{id}/operadores")
	@ResponseStatus(HttpStatus.CREATED)
	public OperadorResponse cadastrarOperador(
			@PathVariable UUID id,
			@Valid @RequestBody CadastroOperadorRequest request
	) {
		return larService.cadastrarOperador(id, request);
	}

	@GetMapping("/me/solicitacoes")
	public List<SolicitacaoAdocaoResponse> listarSolicitacoes(
			@RequestHeader("X-Usuario-Id") UUID usuarioId,
			@RequestParam(required = false) SolicitacaoStatus status
	) {
		return solicitacaoService.listarDoLar(usuarioId, status);
	}
}
