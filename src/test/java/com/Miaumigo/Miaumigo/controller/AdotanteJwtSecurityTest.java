package com.Miaumigo.Miaumigo.controller;

import com.Miaumigo.Miaumigo.domain.SolicitacaoStatus;
import com.Miaumigo.Miaumigo.dto.MensagemSuporteResponse;
import com.Miaumigo.Miaumigo.dto.SolicitacaoAdocaoResponse;
import com.Miaumigo.Miaumigo.domain.MensagemSuporteStatus;
import com.Miaumigo.Miaumigo.service.MensagemSuporteService;
import com.Miaumigo.Miaumigo.service.MatchmakingService;
import com.Miaumigo.Miaumigo.service.SolicitacaoAdocaoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdotanteJwtSecurityTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JwtEncoder jwtEncoder;

	@MockitoBean
	private MatchmakingService matchmakingService;

	@MockitoBean
	private SolicitacaoAdocaoService solicitacaoService;

	@MockitoBean
	private MensagemSuporteService mensagemSuporteService;

	@Test
	void deveRetornarUnauthorized_quandoRecomendacoesSemToken() throws Exception {
		mockMvc.perform(get("/api/v1/adotantes/me/animais-recomendados"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void deveRetornarForbidden_quandoTokenNaoForDeAdotante() throws Exception {
		UUID usuarioId = UUID.randomUUID();

		mockMvc.perform(get("/api/v1/adotantes/me/animais-recomendados")
						.header("Authorization", "Bearer " + token(usuarioId, "OPERADOR")))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.mensagem").value("Acesso permitido apenas para adotantes."));
	}

	@Test
	void deveSolicitarAdocao_quandoTokenForDeAdotante() throws Exception {
		UUID animalId = UUID.fromString("11111111-1111-1111-1111-111111111111");
		UUID adotanteId = UUID.fromString("22222222-2222-2222-2222-222222222222");
		when(solicitacaoService.criar(animalId, adotanteId)).thenReturn(new SolicitacaoAdocaoResponse(
				UUID.randomUUID(),
				animalId,
				"Luna",
				adotanteId,
				"Maria",
				SolicitacaoStatus.PENDENTE,
				LocalDateTime.now(),
				LocalDateTime.now()
		));

		mockMvc.perform(post("/api/v1/animais/{id}/solicitacoes", animalId)
						.header("Authorization", "Bearer " + token(adotanteId, "ADOTANTE")))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.status").value("PENDENTE"));

		verify(solicitacaoService).criar(animalId, adotanteId);
	}

	@Test
	void deveListarSolicitacoes_quandoTokenForDeAdotante() throws Exception {
		UUID adotanteId = UUID.fromString("22222222-2222-2222-2222-222222222222");
		when(solicitacaoService.listarDoAdotante(adotanteId)).thenReturn(List.of());

		mockMvc.perform(get("/api/v1/adotantes/me/solicitacoes")
						.header("Authorization", "Bearer " + token(adotanteId, "ADOTANTE")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray());

		verify(solicitacaoService).listarDoAdotante(adotanteId);
	}

	@Test
	void deveRetornarUnauthorized_quandoMensagemSuporteSemToken() throws Exception {
		mockMvc.perform(post("/api/v1/adotantes/me/suporte")
						.contentType("application/json")
						.content(requestSuporte()))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void deveRetornarForbidden_quandoMensagemSuporteTokenNaoForDeAdotante() throws Exception {
		UUID usuarioId = UUID.randomUUID();

		mockMvc.perform(post("/api/v1/adotantes/me/suporte")
						.header("Authorization", "Bearer " + token(usuarioId, "OPERADOR"))
						.contentType("application/json")
						.content(requestSuporte()))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.mensagem").value("Acesso permitido apenas para adotantes."));
	}

	@Test
	void deveCriarMensagemSuporte_quandoTokenForDeAdotante() throws Exception {
		UUID adotanteId = UUID.fromString("22222222-2222-2222-2222-222222222222");
		UUID mensagemId = UUID.fromString("33333333-3333-3333-3333-333333333333");
		when(mensagemSuporteService.criar(any(), any())).thenReturn(new MensagemSuporteResponse(
				mensagemId,
				"Dúvida sobre adoção",
				"Gostaria de entender melhor o processo.",
				MensagemSuporteStatus.NOVA,
				LocalDateTime.now()
		));

		mockMvc.perform(post("/api/v1/adotantes/me/suporte")
						.header("Authorization", "Bearer " + token(adotanteId, "ADOTANTE"))
						.contentType("application/json")
						.content(requestSuporte()))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.status").value("NOVA"));

		verify(mensagemSuporteService).criar(any(), any());
	}

	private String token(UUID usuarioId, String papel) {
		Instant agora = Instant.now();
		JwtClaimsSet claims = JwtClaimsSet.builder()
				.subject("usuario@email.com")
				.issuedAt(agora)
				.expiresAt(agora.plusSeconds(3600))
				.claim("usuario_id", usuarioId.toString())
				.claim("papel", papel)
				.build();
		JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
		return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
	}

	private String requestSuporte() {
		return """
				{
					"assunto": "Dúvida sobre adoção",
					"mensagem": "Gostaria de entender melhor o processo."
				}
				""";
	}
}
