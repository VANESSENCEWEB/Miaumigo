package com.Miaumigo.Miaumigo.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class JwtService {

	private final JwtEncoder jwtEncoder;
	private final long expirationMs;

	public JwtService(
			JwtEncoder jwtEncoder,
			@Value("${jwt.expiration-ms}") long expirationMs
	) {
		this.jwtEncoder = jwtEncoder;
		this.expirationMs = expirationMs;
	}

	public TokenGerado gerarToken(UUID usuarioId, String email, String papel) {
		Instant emitidoEm = Instant.now();
		Instant expiraEm = emitidoEm.plusMillis(expirationMs);
		JwtClaimsSet claims = JwtClaimsSet.builder()
				.subject(email)
				.issuedAt(emitidoEm)
				.expiresAt(expiraEm)
				.claim("usuario_id", usuarioId.toString())
				.claim("papel", papel)
				.build();

		JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
		String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
		return new TokenGerado(token, expiraEm);
	}

	public record TokenGerado(String token, Instant expiraEm) {
	}
}
