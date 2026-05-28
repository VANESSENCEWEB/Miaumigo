package com.Miaumigo.Miaumigo.controller;

import com.Miaumigo.Miaumigo.dto.LoginRequest;
import com.Miaumigo.Miaumigo.dto.LoginResponse;
import com.Miaumigo.Miaumigo.dto.UsuarioAutenticadoResponse;
import com.Miaumigo.Miaumigo.exception.CredenciaisInvalidasException;
import com.Miaumigo.Miaumigo.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private AuthService authService;

	@Test
	void deveRetornarToken_quandoCredenciaisValidas() throws Exception {
		UUID usuarioId = UUID.fromString("22222222-2222-2222-2222-222222222222");
		when(authService.login(any())).thenReturn(new LoginResponse(
				"jwt-token",
				"Bearer",
				Instant.parse("2026-05-27T12:00:00Z"),
				new UsuarioAutenticadoResponse(usuarioId, "Maria Silva", "maria@email.com", "ADOTANTE")
		));

		mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestValida()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.access_token").value("jwt-token"))
				.andExpect(jsonPath("$.token_type").value("Bearer"))
				.andExpect(jsonPath("$.expira_em").value("2026-05-27T12:00:00Z"))
				.andExpect(jsonPath("$.usuario.id").value(usuarioId.toString()))
				.andExpect(jsonPath("$.usuario.nome").value("Maria Silva"))
				.andExpect(jsonPath("$.usuario.email").value("maria@email.com"))
				.andExpect(jsonPath("$.usuario.papel").value("ADOTANTE"));

		verify(authService).login(any(LoginRequest.class));
	}

	@Test
	void deveRetornarBadRequest_quandoRequestInvalida() throws Exception {
		String request = """
				{
					"email": "",
					"senha": ""
				}
				""";

		mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(request))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.mensagem").value("Dados inválidos"))
				.andExpect(jsonPath("$.erros").isArray());
	}

	@Test
	void deveRetornarUnauthorized_quandoCredenciaisInvalidas() throws Exception {
		doThrow(new CredenciaisInvalidasException())
				.when(authService).login(any());

		mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestValida()))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.mensagem").value("Credenciais inválidas."))
				.andExpect(jsonPath("$.erros").isArray());
	}

	private String requestValida() {
		return """
				{
					"email": "maria@email.com",
					"senha": "senha123"
				}
				""";
	}
}
