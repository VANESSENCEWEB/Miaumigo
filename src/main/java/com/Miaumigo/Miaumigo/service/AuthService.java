package com.Miaumigo.Miaumigo.service;

import com.Miaumigo.Miaumigo.domain.Adotante;
import com.Miaumigo.Miaumigo.domain.Operador;
import com.Miaumigo.Miaumigo.domain.Usuario;
import com.Miaumigo.Miaumigo.dto.LoginRequest;
import com.Miaumigo.Miaumigo.dto.LoginResponse;
import com.Miaumigo.Miaumigo.dto.UsuarioAutenticadoResponse;
import com.Miaumigo.Miaumigo.exception.CredenciaisInvalidasException;
import com.Miaumigo.Miaumigo.repository.UsuarioRepository;
import com.Miaumigo.Miaumigo.security.JwtService;
import com.Miaumigo.Miaumigo.security.JwtService.TokenGerado;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

	private static final String PAPEL_ADOTANTE = "ADOTANTE";
	private static final String TOKEN_TYPE = "Bearer";

	private final UsuarioRepository usuarioRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;

	public AuthService(
			UsuarioRepository usuarioRepository,
			PasswordEncoder passwordEncoder,
			JwtService jwtService
	) {
		this.usuarioRepository = usuarioRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
	}

	@Transactional(readOnly = true)
	public LoginResponse login(LoginRequest request) {
		String email = request.email().trim().toLowerCase();
		Usuario usuario = usuarioRepository.findByEmail(email)
				.orElseThrow(CredenciaisInvalidasException::new);

		if (!senhaValida(request.senha(), usuario.getSenha())) {
			throw new CredenciaisInvalidasException();
		}

		String papel = papelDoUsuario(usuario);
		TokenGerado token = jwtService.gerarToken(usuario.getId(), usuario.getEmail(), papel);
		return new LoginResponse(
				token.token(),
				TOKEN_TYPE,
				token.expiraEm(),
				new UsuarioAutenticadoResponse(
						usuario.getId(),
						usuario.getNome(),
						usuario.getEmail(),
						papel
				)
		);
	}

	private String papelDoUsuario(Usuario usuario) {
		if (usuario instanceof Adotante) {
			return PAPEL_ADOTANTE;
		}
		if (usuario instanceof Operador operador) {
			return operador.getRole().name();
		}
		throw new CredenciaisInvalidasException();
	}

	private boolean senhaValida(String senhaInformada, String senhaSalva) {
		if (senhaSalva == null || senhaSalva.isBlank()) {
			return false;
		}
		if (senhaSalva.startsWith("$2a$") || senhaSalva.startsWith("$2b$") || senhaSalva.startsWith("$2y$")) {
			return passwordEncoder.matches(senhaInformada, senhaSalva);
		}
		return senhaSalva.equals(senhaInformada);
	}
}
