package com.Miaumigo.Miaumigo.security;

import com.Miaumigo.Miaumigo.exception.AcessoNegadoException;
import com.Miaumigo.Miaumigo.exception.IdentidadeNaoAutenticadaException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UsuarioAutenticadoService {

	private static final String PAPEL_ADOTANTE = "ADOTANTE";
	private static final String PAPEL_OPERADOR = "OPERADOR";
	private static final String PAPEL_ADMIN = "ADMIN";

	public UUID exigirAdotante(Jwt jwt) {
		return exigirPapel(jwt, PAPEL_ADOTANTE, "Acesso permitido apenas para adotantes.");
	}

	public UUID exigirOperador(Jwt jwt) {
		return exigirPapel(jwt, PAPEL_OPERADOR, "Acesso permitido apenas para operadores.");
	}

	public UUID exigirAdmin(Jwt jwt) {
		return exigirPapel(jwt, PAPEL_ADMIN, "Acesso permitido apenas para administradores.");
	}

	public UUID exigirOperadorOuAdmin(Jwt jwt) {
		if (jwt == null) {
			throw new IdentidadeNaoAutenticadaException("Usuário não autenticado.");
		}
		String papel = jwt.getClaimAsString("papel");
		if (!PAPEL_OPERADOR.equals(papel) && !PAPEL_ADMIN.equals(papel)) {
			throw new AcessoNegadoException("Acesso permitido apenas para operadores ou administradores.");
		}
		return usuarioId(jwt);
	}

	private UUID exigirPapel(Jwt jwt, String papelEsperado, String mensagemAcessoNegado) {
		if (jwt == null) {
			throw new IdentidadeNaoAutenticadaException("Usuário não autenticado.");
		}
		String papel = jwt.getClaimAsString("papel");
		if (!papelEsperado.equals(papel)) {
			throw new AcessoNegadoException(mensagemAcessoNegado);
		}
		return usuarioId(jwt);
	}

	private UUID usuarioId(Jwt jwt) {
		String usuarioId = jwt.getClaimAsString("usuario_id");
		try {
			return UUID.fromString(usuarioId);
		} catch (RuntimeException exception) {
			throw new IdentidadeNaoAutenticadaException("Token sem identidade válida.");
		}
	}
}
