package com.Miaumigo.Miaumigo.controller;

import com.Miaumigo.Miaumigo.dto.SolicitacaoAdocaoResponse;
import com.Miaumigo.Miaumigo.security.UsuarioAutenticadoService;
import com.Miaumigo.Miaumigo.service.SolicitacaoAdocaoService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/solicitacoes")
public class SolicitacaoController {

	private final SolicitacaoAdocaoService solicitacaoService;
	private final UsuarioAutenticadoService usuarioAutenticadoService;

	public SolicitacaoController(
			SolicitacaoAdocaoService solicitacaoService,
			UsuarioAutenticadoService usuarioAutenticadoService
	) {
		this.solicitacaoService = solicitacaoService;
		this.usuarioAutenticadoService = usuarioAutenticadoService;
	}

	@PostMapping("/{id}/cancelamento")
	public SolicitacaoAdocaoResponse cancelar(
			@PathVariable UUID id,
			@AuthenticationPrincipal Jwt jwt
	) {
		return solicitacaoService.cancelar(id, usuarioAutenticadoService.exigirAdotante(jwt));
	}

	@PostMapping("/{id}/aprovacao")
	public SolicitacaoAdocaoResponse aprovar(
			@PathVariable UUID id,
			@AuthenticationPrincipal Jwt jwt
	) {
		return solicitacaoService.aprovar(id, usuarioAutenticadoService.exigirOperador(jwt));
	}

	@PostMapping("/{id}/rejeicao")
	public SolicitacaoAdocaoResponse rejeitar(
			@PathVariable UUID id,
			@AuthenticationPrincipal Jwt jwt
	) {
		return solicitacaoService.rejeitar(id, usuarioAutenticadoService.exigirOperador(jwt));
	}
}
