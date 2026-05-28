package com.Miaumigo.Miaumigo.controller;

import com.Miaumigo.Miaumigo.domain.SolicitacaoStatus;
import com.Miaumigo.Miaumigo.domain.Role;
import com.Miaumigo.Miaumigo.dto.CadastroLarComOperadorResponse;
import com.Miaumigo.Miaumigo.dto.LarResponse;
import com.Miaumigo.Miaumigo.dto.OperadorResponse;
import com.Miaumigo.Miaumigo.dto.TextoDivulgacaoResponse;
import com.Miaumigo.Miaumigo.dto.SolicitacaoAdocaoResponse;
import com.Miaumigo.Miaumigo.service.AnimalService;
import com.Miaumigo.Miaumigo.service.LarService;
import com.Miaumigo.Miaumigo.service.SolicitacaoAdocaoService;
import com.Miaumigo.Miaumigo.service.TextoDivulgacaoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
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
class OperadorJwtSecurityTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JwtEncoder jwtEncoder;

	@MockitoBean
	private AnimalService animalService;

	@MockitoBean
	private LarService larService;

	@MockitoBean
	private SolicitacaoAdocaoService solicitacaoService;

	@MockitoBean
	private TextoDivulgacaoService textoDivulgacaoService;

	@Test
	void deveRetornarUnauthorized_quandoSolicitacoesDoLarSemToken() throws Exception {
		mockMvc.perform(get("/api/v1/lares/me/solicitacoes"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void deveRetornarUnauthorized_quandoCadastrarLarSemToken() throws Exception {
		mockMvc.perform(post("/api/v1/lares")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"nome\":\"Lar Amigo\"}"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void deveCadastrarLarComOperadorSemToken_quandoDadosValidos() throws Exception {
		UUID larId = UUID.fromString("55555555-5555-5555-5555-555555555555");
		UUID operadorId = UUID.fromString("33333333-3333-3333-3333-333333333333");
		when(larService.cadastrarComOperador(any()))
				.thenReturn(new CadastroLarComOperadorResponse(
						new LarResponse(larId, "Lar Amigo"),
						new OperadorResponse(operadorId, "Ana", "ana@email.com", Role.OPERADOR, larId)
				));

		mockMvc.perform(post("/api/v1/lares/cadastro")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
									"nome": "Lar Amigo",
									"operador": {
										"nome": "Ana",
										"endereco": "Rua A",
										"email": "ana@email.com",
										"senha": "senha",
										"cpf": "12345678901"
									}
								}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.lar.id").value(larId.toString()))
				.andExpect(jsonPath("$.operador.id").value(operadorId.toString()));

		verify(larService).cadastrarComOperador(any());
	}

	@Test
	void deveRetornarForbidden_quandoOperadorCadastrarLar() throws Exception {
		mockMvc.perform(post("/api/v1/lares")
						.header("Authorization", "Bearer " + token(UUID.randomUUID(), "OPERADOR"))
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"nome\":\"Lar Amigo\"}"))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.mensagem").value("Acesso permitido apenas para administradores."));
	}

	@Test
	void deveCadastrarLar_quandoTokenForDeAdmin() throws Exception {
		UUID larId = UUID.fromString("55555555-5555-5555-5555-555555555555");
		when(larService.cadastrar(any())).thenReturn(new LarResponse(larId, "Lar Amigo"));

		mockMvc.perform(post("/api/v1/lares")
						.header("Authorization", "Bearer " + token(UUID.randomUUID(), "ADMIN"))
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"nome\":\"Lar Amigo\"}"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(larId.toString()));

		verify(larService).cadastrar(any());
	}

	@Test
	void deveRetornarForbidden_quandoOperadorCadastrarOperador() throws Exception {
		mockMvc.perform(post("/api/v1/lares/{id}/operadores", UUID.randomUUID())
						.header("Authorization", "Bearer " + token(UUID.randomUUID(), "OPERADOR"))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"nome":"Ana","endereco":"Rua A","email":"ana@email.com","senha":"senha","cpf":"12345678901"}
								"""))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.mensagem").value("Acesso permitido apenas para administradores."));
	}

	@Test
	void deveCadastrarOperador_quandoTokenForDeAdmin() throws Exception {
		UUID larId = UUID.fromString("55555555-5555-5555-5555-555555555555");
		UUID operadorId = UUID.fromString("33333333-3333-3333-3333-333333333333");
		when(larService.cadastrarOperador(any(), any()))
				.thenReturn(new OperadorResponse(operadorId, "Ana", "ana@email.com", Role.OPERADOR, larId));

		mockMvc.perform(post("/api/v1/lares/{id}/operadores", larId)
						.header("Authorization", "Bearer " + token(UUID.randomUUID(), "ADMIN"))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"nome":"Ana","endereco":"Rua A","email":"ana@email.com","senha":"senha","cpf":"12345678901"}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(operadorId.toString()));

		verify(larService).cadastrarOperador(org.mockito.ArgumentMatchers.eq(larId), any());
	}

	@Test
	void deveRetornarForbidden_quandoTokenNaoForDeOperador() throws Exception {
		UUID usuarioId = UUID.randomUUID();

		mockMvc.perform(get("/api/v1/lares/me/solicitacoes")
						.header("Authorization", "Bearer " + token(usuarioId, "ADOTANTE")))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.mensagem").value("Acesso permitido apenas para operadores."));
	}

	@Test
	void deveListarSolicitacoesDoLar_quandoTokenForDeOperador() throws Exception {
		UUID operadorId = UUID.fromString("33333333-3333-3333-3333-333333333333");
		when(solicitacaoService.listarDoLar(operadorId, SolicitacaoStatus.PENDENTE)).thenReturn(List.of());

		mockMvc.perform(get("/api/v1/lares/me/solicitacoes")
						.header("Authorization", "Bearer " + token(operadorId, "OPERADOR"))
						.param("status", "PENDENTE"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray());

		verify(solicitacaoService).listarDoLar(operadorId, SolicitacaoStatus.PENDENTE);
	}

	@Test
	void deveAprovarSolicitacao_quandoTokenForDeOperador() throws Exception {
		UUID solicitacaoId = UUID.fromString("44444444-4444-4444-4444-444444444444");
		UUID operadorId = UUID.fromString("33333333-3333-3333-3333-333333333333");
		when(solicitacaoService.aprovar(solicitacaoId, operadorId))
				.thenReturn(response(SolicitacaoStatus.APROVADA));

		mockMvc.perform(post("/api/v1/solicitacoes/{id}/aprovacao", solicitacaoId)
						.header("Authorization", "Bearer " + token(operadorId, "OPERADOR")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("APROVADA"));

		verify(solicitacaoService).aprovar(solicitacaoId, operadorId);
	}

	@Test
	void deveCadastrarAnimal_quandoTokenForDeOperador() throws Exception {
		UUID operadorId = UUID.fromString("33333333-3333-3333-3333-333333333333");

		mockMvc.perform(post("/api/v1/animais")
						.header("Authorization", "Bearer " + token(operadorId, "OPERADOR"))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
									"nome": "Luna",
									"especie": "GATO",
									"porte": "PEQUENO",
									"idade": 2,
									"descricao": "Docil e tranquila",
									"tags": ["CALMO"]
								}
								"""))
				.andExpect(status().isCreated());

		verify(animalService).cadastrar(any(), org.mockito.ArgumentMatchers.eq(operadorId));
	}

	@Test
	void deveRetornarUnauthorized_quandoGerarTextoDivulgacaoSemToken() throws Exception {
		mockMvc.perform(post("/api/v1/animais/{id}/texto-divulgacao", UUID.randomUUID()))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void deveRetornarForbidden_quandoAdotanteGerarTextoDivulgacao() throws Exception {
		mockMvc.perform(post("/api/v1/animais/{id}/texto-divulgacao", UUID.randomUUID())
						.header("Authorization", "Bearer " + token(UUID.randomUUID(), "ADOTANTE")))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.mensagem").value("Acesso permitido apenas para operadores ou administradores."));
	}

	@Test
	void deveGerarTextoDivulgacao_quandoTokenForDeOperador() throws Exception {
		UUID animalId = UUID.fromString("11111111-1111-1111-1111-111111111111");
		when(textoDivulgacaoService.gerar(animalId))
				.thenReturn(new TextoDivulgacaoResponse("Luna procura uma familia."));

		mockMvc.perform(post("/api/v1/animais/{id}/texto-divulgacao", animalId)
						.header("Authorization", "Bearer " + token(UUID.randomUUID(), "OPERADOR")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.texto").value("Luna procura uma familia."));

		verify(textoDivulgacaoService).gerar(animalId);
	}

	private SolicitacaoAdocaoResponse response(SolicitacaoStatus status) {
		return new SolicitacaoAdocaoResponse(
				UUID.randomUUID(),
				UUID.randomUUID(),
				"Luna",
				UUID.randomUUID(),
				"Maria",
				status,
				LocalDateTime.now(),
				LocalDateTime.now()
		);
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
}
