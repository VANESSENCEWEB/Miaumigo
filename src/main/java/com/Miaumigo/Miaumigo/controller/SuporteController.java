package com.Miaumigo.Miaumigo.controller;

import com.Miaumigo.Miaumigo.dto.MensagemSuporteRequest;
import com.Miaumigo.Miaumigo.dto.MensagemSuporteResponse;
import com.Miaumigo.Miaumigo.security.UsuarioAutenticadoService;
import com.Miaumigo.Miaumigo.service.MensagemSuporteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/adotantes/me/suporte")
public class SuporteController {

	private final MensagemSuporteService mensagemSuporteService;
	private final UsuarioAutenticadoService usuarioAutenticadoService;

	public SuporteController(
			MensagemSuporteService mensagemSuporteService,
			UsuarioAutenticadoService usuarioAutenticadoService
	) {
		this.mensagemSuporteService = mensagemSuporteService;
		this.usuarioAutenticadoService = usuarioAutenticadoService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public MensagemSuporteResponse criar(
			@AuthenticationPrincipal Jwt jwt,
			@Valid @RequestBody MensagemSuporteRequest request
	) {
		return mensagemSuporteService.criar(usuarioAutenticadoService.exigirAdotante(jwt), request);
	}
}
