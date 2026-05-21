package com.Miaumigo.Miaumigo.controller;

import com.Miaumigo.Miaumigo.dto.AcaoRealizadaResponse;
import com.Miaumigo.Miaumigo.exception.RecursoNaoEncontradoException;
import com.Miaumigo.Miaumigo.service.AnimalService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AnimalController.class)
class AnimalControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private AnimalService animalService;

	@Test
	void deveCadastrarAnimal_quandoDadosValidos() throws Exception {
		String request = """
				{
					"nome": "Luna",
					"especie": "GATO",
					"porte": "PEQUENO",
					"idade": 2,
					"descricao": "Dócil e tranquila",
					"tags": ["dócil", "castrada"],
					"cloudinary_public_id": "animais/luna",
					"lar_id": "550e8400-e29b-41d4-a716-446655440000"
				}
				""";

		mockMvc.perform(post("/api/v1/animais")
						.contentType(MediaType.APPLICATION_JSON)
						.content(request))
				.andExpect(status().isCreated())
				.andExpect(content().string(""));

		verify(animalService).cadastrar(any());
	}

	@Test
	void deveRetornarBadRequest_quandoRequestInvalida() throws Exception {
		String request = """
				{
					"nome": "",
					"especie": "GATO",
					"porte": "PEQUENO",
					"idade": -1,
					"lar_id": "550e8400-e29b-41d4-a716-446655440000"
				}
				""";

		mockMvc.perform(post("/api/v1/animais")
						.contentType(MediaType.APPLICATION_JSON)
						.content(request))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.mensagem").value("Dados inválidos"))
				.andExpect(jsonPath("$.erros").isArray());
	}

	@Test
	void deveRetornarBadRequest_quandoRegraDeDominioFalhar() throws Exception {
		String request = """
				{
					"nome": "Luna",
					"especie": "GATO",
					"porte": "PEQUENO",
					"idade": 2,
					"descricao": "Dócil e tranquila",
					"tags": ["dócil"],
					"cloudinary_public_id": "animais/luna",
					"lar_id": "550e8400-e29b-41d4-a716-446655440000"
				}
				""";
		doThrow(new IllegalArgumentException("Nome do animal é obrigatório."))
				.when(animalService).cadastrar(any());

		mockMvc.perform(post("/api/v1/animais")
						.contentType(MediaType.APPLICATION_JSON)
						.content(request))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.mensagem").value("Nome do animal é obrigatório."))
				.andExpect(jsonPath("$.erros").isArray());
	}

	@Test
	void deveRetornarInternalServerError_quandoCadastroFalharInesperadamente() throws Exception {
		String request = """
				{
					"nome": "Luna",
					"especie": "GATO",
					"porte": "PEQUENO",
					"idade": 2,
					"descricao": "Dócil e tranquila",
					"tags": ["dócil"],
					"cloudinary_public_id": "animais/luna",
					"lar_id": "550e8400-e29b-41d4-a716-446655440000"
				}
				""";
		doThrow(new RuntimeException("Banco indisponível"))
				.when(animalService).cadastrar(any());

		mockMvc.perform(post("/api/v1/animais")
						.contentType(MediaType.APPLICATION_JSON)
						.content(request))
				.andExpect(status().isInternalServerError())
				.andExpect(jsonPath("$.mensagem").value("Erro interno no servidor"))
				.andExpect(jsonPath("$.erros").isArray());
	}

	@Test
	void deveRealizarAdocao_quandoDadosValidos() throws Exception {
		UUID id = UUID.fromString("11111111-1111-1111-1111-111111111111");
		String request = """
				{
					"adotado_por": "Maria Silva"
				}
				""";
		when(animalService.realizarAdocao(any(), any()))
				.thenReturn(new AcaoRealizadaResponse("Adoção realizada com sucesso."));

		mockMvc.perform(post("/api/v1/animais/{id}/adocao", id)
						.contentType(MediaType.APPLICATION_JSON)
						.content(request))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.mensagem").value("Adoção realizada com sucesso."));

		verify(animalService).realizarAdocao(any(), any());
	}

	@Test
	void deveRetornarBadRequest_quandoAdocaoSemResponsavel() throws Exception {
		UUID id = UUID.fromString("11111111-1111-1111-1111-111111111111");
		String request = """
				{
					"adotado_por": ""
				}
				""";

		mockMvc.perform(post("/api/v1/animais/{id}/adocao", id)
						.contentType(MediaType.APPLICATION_JSON)
						.content(request))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.mensagem").value("Dados inválidos"))
				.andExpect(jsonPath("$.erros").isArray());
	}

	@Test
	void deveRetornarNotFound_quandoAnimalNaoEncontradoParaAdocao() throws Exception {
		UUID id = UUID.fromString("11111111-1111-1111-1111-111111111111");
		String request = """
				{
					"adotado_por": "Maria Silva"
				}
				""";
		doThrow(new RecursoNaoEncontradoException("Animal não encontrado."))
				.when(animalService).realizarAdocao(any(), any());

		mockMvc.perform(post("/api/v1/animais/{id}/adocao", id)
						.contentType(MediaType.APPLICATION_JSON)
						.content(request))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.mensagem").value("Animal não encontrado."))
				.andExpect(jsonPath("$.erros").isArray());
	}
}
