package com.Miaumigo.Miaumigo.controller;

import com.Miaumigo.Miaumigo.dto.SolicitacaoAdocaoResponse;
import com.Miaumigo.Miaumigo.service.SolicitacaoAdocaoService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/solicitacoes")
public class SolicitacaoController {

	private final SolicitacaoAdocaoService solicitacaoService;

	public SolicitacaoController(SolicitacaoAdocaoService solicitacaoService) {
		this.solicitacaoService = solicitacaoService;
	}

	@PostMapping("/{id}/cancelamento")
	public SolicitacaoAdocaoResponse cancelar(
			@PathVariable UUID id,
			@RequestHeader("X-Usuario-Id") UUID usuarioId
	) {
		return solicitacaoService.cancelar(id, usuarioId);
	}

	@PostMapping("/{id}/aprovacao")
	public SolicitacaoAdocaoResponse aprovar(
			@PathVariable UUID id,
			@RequestHeader("X-Usuario-Id") UUID usuarioId
	) {
		return solicitacaoService.aprovar(id, usuarioId);
	}

	@PostMapping("/{id}/rejeicao")
	public SolicitacaoAdocaoResponse rejeitar(
			@PathVariable UUID id,
			@RequestHeader("X-Usuario-Id") UUID usuarioId
	) {
		return solicitacaoService.rejeitar(id, usuarioId);
	}
}
