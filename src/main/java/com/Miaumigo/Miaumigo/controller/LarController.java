package com.Miaumigo.Miaumigo.controller;

import com.Miaumigo.Miaumigo.domain.SolicitacaoStatus;
import com.Miaumigo.Miaumigo.dto.CadastroLarComOperadorRequest;
import com.Miaumigo.Miaumigo.dto.CadastroLarComOperadorResponse;
import com.Miaumigo.Miaumigo.dto.CadastroLarRequest;
import com.Miaumigo.Miaumigo.dto.CadastroOperadorRequest;
import com.Miaumigo.Miaumigo.dto.LarResponse;
import com.Miaumigo.Miaumigo.dto.OperadorResponse;
import com.Miaumigo.Miaumigo.dto.SolicitacaoAdocaoResponse;
import com.Miaumigo.Miaumigo.security.UsuarioAutenticadoService;
import com.Miaumigo.Miaumigo.service.LarService;
import com.Miaumigo.Miaumigo.service.SolicitacaoAdocaoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
	private final UsuarioAutenticadoService usuarioAutenticadoService;

	public LarController(
			LarService larService,
			SolicitacaoAdocaoService solicitacaoService,
			UsuarioAutenticadoService usuarioAutenticadoService
	) {
		this.larService = larService;
		this.solicitacaoService = solicitacaoService;
		this.usuarioAutenticadoService = usuarioAutenticadoService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public LarResponse cadastrar(
			@Valid @RequestBody CadastroLarRequest request,
			@AuthenticationPrincipal Jwt jwt
	) {
		usuarioAutenticadoService.exigirAdmin(jwt);
		return larService.cadastrar(request);
	}

	@PostMapping("/cadastro")
	@ResponseStatus(HttpStatus.CREATED)
	public CadastroLarComOperadorResponse cadastrarComOperador(
			@Valid @RequestBody CadastroLarComOperadorRequest request
	) {
		return larService.cadastrarComOperador(request);
	}

	@PostMapping("/{id}/operadores")
	@ResponseStatus(HttpStatus.CREATED)
	public OperadorResponse cadastrarOperador(
			@PathVariable UUID id,
			@Valid @RequestBody CadastroOperadorRequest request,
			@AuthenticationPrincipal Jwt jwt
	) {
		usuarioAutenticadoService.exigirAdmin(jwt);
		return larService.cadastrarOperador(id, request);
	}

	@GetMapping("/me/solicitacoes")
	public List<SolicitacaoAdocaoResponse> listarSolicitacoes(
			@AuthenticationPrincipal Jwt jwt,
			@RequestParam(required = false) SolicitacaoStatus status
	) {
		return solicitacaoService.listarDoLar(usuarioAutenticadoService.exigirOperador(jwt), status);
	}
}
