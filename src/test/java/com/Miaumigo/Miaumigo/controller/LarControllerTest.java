package com.Miaumigo.Miaumigo.controller;

import com.Miaumigo.Miaumigo.domain.Role;
import com.Miaumigo.Miaumigo.domain.SolicitacaoStatus;
import com.Miaumigo.Miaumigo.dto.LarResponse;
import com.Miaumigo.Miaumigo.dto.OperadorResponse;
import com.Miaumigo.Miaumigo.service.LarService;
import com.Miaumigo.Miaumigo.service.SolicitacaoAdocaoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LarController.class)
class LarControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private LarService larService;

	@MockitoBean
	private SolicitacaoAdocaoService solicitacaoService;

	@Test
	void deveCadastrarLar_quandoNomeValido() throws Exception {
		when(larService.cadastrar(any())).thenReturn(new LarResponse(UUID.randomUUID(), "Lar Amigo"));

		mockMvc.perform(post("/api/v1/lares")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"nome\":\"Lar Amigo\"}"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.nome").value("Lar Amigo"));
	}

	@Test
	void deveCadastrarOperador_quandoDadosValidos() throws Exception {
		UUID larId = UUID.randomUUID();
		when(larService.cadastrarOperador(any(), any()))
				.thenReturn(new OperadorResponse(UUID.randomUUID(), "Ana", "ana@email.com", Role.OPERADOR, larId));

		mockMvc.perform(post("/api/v1/lares/{id}/operadores", larId)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"nome":"Ana","endereco":"Rua A","email":"ana@email.com","senha":"senha","cpf":"12345678901"}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.role").value("OPERADOR"));
	}

	@Test
	void deveListarSolicitacoes_quandoOperadorInformado() throws Exception {
		UUID operadorId = UUID.randomUUID();
		when(solicitacaoService.listarDoLar(operadorId, SolicitacaoStatus.PENDENTE)).thenReturn(List.of());

		mockMvc.perform(get("/api/v1/lares/me/solicitacoes")
						.header("X-Usuario-Id", operadorId)
						.param("status", "PENDENTE"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray());
	}
}
