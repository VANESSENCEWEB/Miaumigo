package com.Miaumigo.Miaumigo.controller;

import com.Miaumigo.Miaumigo.domain.MensagemSuporteStatus;
import com.Miaumigo.Miaumigo.dto.MensagemSuporteResponse;
import com.Miaumigo.Miaumigo.security.UsuarioAutenticadoService;
import com.Miaumigo.Miaumigo.service.MensagemSuporteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SuporteController.class)
@AutoConfigureMockMvc(addFilters = false)
class SuporteControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private MensagemSuporteService mensagemSuporteService;

	@MockitoBean
	private UsuarioAutenticadoService usuarioAutenticadoService;

	@Test
	void deveCriarMensagemSuporte_quandoRequestValida() throws Exception {
		UUID adotanteId = UUID.fromString("22222222-2222-2222-2222-222222222222");
		UUID mensagemId = UUID.fromString("33333333-3333-3333-3333-333333333333");
		when(usuarioAutenticadoService.exigirAdotante(any())).thenReturn(adotanteId);
		when(mensagemSuporteService.criar(any(), any()))
				.thenReturn(new MensagemSuporteResponse(
						mensagemId,
						"Dúvida sobre adoção",
						"Gostaria de entender melhor o processo.",
						MensagemSuporteStatus.NOVA,
						LocalDateTime.of(2026, 6, 1, 10, 30)
				));

		mockMvc.perform(post("/api/v1/adotantes/me/suporte")
						.header("Authorization", "Bearer token")
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestValida()))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(mensagemId.toString()))
				.andExpect(jsonPath("$.assunto").value("Dúvida sobre adoção"))
				.andExpect(jsonPath("$.mensagem").value("Gostaria de entender melhor o processo."))
				.andExpect(jsonPath("$.status").value("NOVA"))
				.andExpect(jsonPath("$.criado_em").exists());

		verify(mensagemSuporteService).criar(any(), any());
	}

	@Test
	void deveRetornarBadRequest_quandoMensagemForInvalida() throws Exception {
		String request = """
				{
					"assunto": "",
					"mensagem": "curta"
				}
				""";

		mockMvc.perform(post("/api/v1/adotantes/me/suporte")
						.contentType(MediaType.APPLICATION_JSON)
						.content(request))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.mensagem").value("Dados inválidos"))
				.andExpect(jsonPath("$.erros").isArray());
	}

	private String requestValida() {
		return """
				{
					"assunto": "Dúvida sobre adoção",
					"mensagem": "Gostaria de entender melhor o processo."
				}
				""";
	}
}
