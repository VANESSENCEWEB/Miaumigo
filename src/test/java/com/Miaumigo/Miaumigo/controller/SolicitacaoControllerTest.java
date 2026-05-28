package com.Miaumigo.Miaumigo.controller;

import com.Miaumigo.Miaumigo.domain.SolicitacaoStatus;
import com.Miaumigo.Miaumigo.dto.SolicitacaoAdocaoResponse;
import com.Miaumigo.Miaumigo.security.UsuarioAutenticadoService;
import com.Miaumigo.Miaumigo.service.SolicitacaoAdocaoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SolicitacaoController.class)
@AutoConfigureMockMvc(addFilters = false)
class SolicitacaoControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private SolicitacaoAdocaoService service;

	@MockitoBean
	private UsuarioAutenticadoService usuarioAutenticadoService;

	@Test
	void deveAprovarSolicitacao_quandoOperadorInformado() throws Exception {
		UUID solicitacaoId = UUID.randomUUID();
		UUID operadorId = UUID.randomUUID();
		when(usuarioAutenticadoService.exigirOperador(any())).thenReturn(operadorId);
		when(service.aprovar(solicitacaoId, operadorId)).thenReturn(response(SolicitacaoStatus.APROVADA));

		mockMvc.perform(post("/api/v1/solicitacoes/{id}/aprovacao", solicitacaoId)
						.header("Authorization", "Bearer token"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("APROVADA"));
	}

	@Test
	void deveCancelarSolicitacao_quandoAdotanteInformado() throws Exception {
		UUID solicitacaoId = UUID.randomUUID();
		UUID adotanteId = UUID.randomUUID();
		when(usuarioAutenticadoService.exigirAdotante(any())).thenReturn(adotanteId);
		when(service.cancelar(solicitacaoId, adotanteId)).thenReturn(response(SolicitacaoStatus.CANCELADA));

		mockMvc.perform(post("/api/v1/solicitacoes/{id}/cancelamento", solicitacaoId)
						.header("Authorization", "Bearer token"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("CANCELADA"));
	}

	private SolicitacaoAdocaoResponse response(SolicitacaoStatus status) {
		return new SolicitacaoAdocaoResponse(
				UUID.randomUUID(), UUID.randomUUID(), "Luna", UUID.randomUUID(), "Maria", status,
				LocalDateTime.now(), LocalDateTime.now()
		);
	}
}
