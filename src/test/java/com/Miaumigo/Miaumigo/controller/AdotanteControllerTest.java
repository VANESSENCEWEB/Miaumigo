package com.Miaumigo.Miaumigo.controller;

import com.Miaumigo.Miaumigo.domain.Tag;
import com.Miaumigo.Miaumigo.dto.AdotanteResponse;
import com.Miaumigo.Miaumigo.dto.AnimalRecomendadoResponse;
import com.Miaumigo.Miaumigo.exception.CpfJaCadastradoException;
import com.Miaumigo.Miaumigo.exception.EmailJaCadastradoException;
import com.Miaumigo.Miaumigo.exception.RecursoNaoEncontradoException;
import com.Miaumigo.Miaumigo.service.AdotanteService;
import com.Miaumigo.Miaumigo.service.MatchmakingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdotanteController.class)
class AdotanteControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private AdotanteService adotanteService;

	@MockitoBean
	private MatchmakingService matchmakingService;

	@Test
	void deveCadastrarAdotante_quandoDadosValidos() throws Exception {
		UUID id = UUID.fromString("22222222-2222-2222-2222-222222222222");
		when(adotanteService.cadastrar(any()))
				.thenReturn(new AdotanteResponse(
						id,
						"Maria Silva",
						"Rua das Flores, 123",
						"maria@email.com",
						"12345678901",
						List.of(Tag.CALMO, Tag.CONVIVE_COM_GATOS)
				));

		mockMvc.perform(post("/api/v1/adotantes")
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestValida()))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(id.toString()))
				.andExpect(jsonPath("$.nome").value("Maria Silva"))
				.andExpect(jsonPath("$.email").value("maria@email.com"))
				.andExpect(jsonPath("$.cpf").value("12345678901"))
				.andExpect(jsonPath("$.preferencias[0]").value("CALMO"))
				.andExpect(jsonPath("$.preferencias[1]").value("CONVIVE_COM_GATOS"));

		verify(adotanteService).cadastrar(any());
	}

	@Test
	void deveRetornarBadRequest_quandoRequestInvalida() throws Exception {
		String request = """
				{
					"nome": "",
					"endereco": "",
					"email": "",
					"senha": "",
					"cpf": ""
				}
				""";

		mockMvc.perform(post("/api/v1/adotantes")
						.contentType(MediaType.APPLICATION_JSON)
						.content(request))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.mensagem").value("Dados inválidos"))
				.andExpect(jsonPath("$.erros").isArray());
	}

	@Test
	void deveRetornarBadRequest_quandoEmailJaCadastrado() throws Exception {
		doThrow(new EmailJaCadastradoException())
				.when(adotanteService).cadastrar(any());

		mockMvc.perform(post("/api/v1/adotantes")
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestValida()))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.mensagem").value("Email já cadastrado."))
				.andExpect(jsonPath("$.erros").isArray());
	}

	@Test
	void deveRetornarBadRequest_quandoCpfJaCadastrado() throws Exception {
		doThrow(new CpfJaCadastradoException())
				.when(adotanteService).cadastrar(any());

		mockMvc.perform(post("/api/v1/adotantes")
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestValida()))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.mensagem").value("CPF já cadastrado."))
				.andExpect(jsonPath("$.erros").isArray());
	}

	@Test
	void deveRetornarAnimaisOrdenados_quandoSolicitarRecomendacoes() throws Exception {
		UUID adotanteId = UUID.fromString("22222222-2222-2222-2222-222222222222");
		UUID lunaId = UUID.fromString("11111111-1111-1111-1111-111111111111");
		UUID thorId = UUID.fromString("33333333-3333-3333-3333-333333333333");
		when(matchmakingService.recomendarAnimais(adotanteId))
				.thenReturn(List.of(
						new AnimalRecomendadoResponse(lunaId, "Luna", 2, null, null, List.of(Tag.CALMO, Tag.CONVIVE_COM_GATOS), "animais/luna", 2),
						new AnimalRecomendadoResponse(thorId, "Thor", 3, null, null, List.of(Tag.ENERGICO), "animais/thor", 0)
				));

		mockMvc.perform(get("/api/v1/adotantes/{id}/animais-recomendados", adotanteId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].nome").value("Luna"))
				.andExpect(jsonPath("$[0].compatibilidade").value(2))
				.andExpect(jsonPath("$[1].nome").value("Thor"))
				.andExpect(jsonPath("$[1].compatibilidade").value(0));

		verify(matchmakingService).recomendarAnimais(adotanteId);
	}

	@Test
	void deveRetornarNotFound_quandoAdotanteNaoExistirNasRecomendacoes() throws Exception {
		UUID adotanteId = UUID.fromString("22222222-2222-2222-2222-222222222222");
		doThrow(new RecursoNaoEncontradoException("Adotante não encontrado."))
				.when(matchmakingService).recomendarAnimais(adotanteId);

		mockMvc.perform(get("/api/v1/adotantes/{id}/animais-recomendados", adotanteId))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.mensagem").value("Adotante não encontrado."));
	}

	private String requestValida() {
		return """
				{
					"nome": "Maria Silva",
					"endereco": "Rua das Flores, 123",
					"email": "maria@email.com",
					"senha": "senha123",
					"cpf": "12345678901",
					"preferencias": ["CALMO", "CONVIVE_COM_GATOS"]
				}
				""";
	}
}
