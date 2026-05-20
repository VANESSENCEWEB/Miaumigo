package com.Miaumigo.Miaumigo.controller;

import com.Miaumigo.Miaumigo.domain.AnimalStatus;
import com.Miaumigo.Miaumigo.domain.Especie;
import com.Miaumigo.Miaumigo.domain.Porte;
import com.Miaumigo.Miaumigo.dto.AnimalResponse;
import com.Miaumigo.Miaumigo.exception.RecursoNaoEncontradoException;
import com.Miaumigo.Miaumigo.service.AnimalService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
	void deveListarTodosAnimais_quandoExistiremAnimaisCadastrados() throws Exception {
		UUID id = UUID.fromString("11111111-1111-1111-1111-111111111111");
		UUID larId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
		LocalDateTime criadoEm = LocalDateTime.of(2026, 5, 18, 10, 0);
		LocalDateTime atualizadoEm = LocalDateTime.of(2026, 5, 18, 11, 0);
		when(animalService.listarTodos()).thenReturn(List.of(new AnimalResponse(
				id,
				"Luna",
				Especie.GATO,
				Porte.PEQUENO,
				2,
				"Dócil e tranquila",
				AnimalStatus.DISPONIVEL,
				larId,
				criadoEm,
				atualizadoEm
		)));

		mockMvc.perform(get("/api/v1/animais"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value(id.toString()))
				.andExpect(jsonPath("$[0].nome").value("Luna"))
				.andExpect(jsonPath("$[0].especie").value("GATO"))
				.andExpect(jsonPath("$[0].porte").value("PEQUENO"))
				.andExpect(jsonPath("$[0].idade").value(2))
				.andExpect(jsonPath("$[0].descricao").value("Dócil e tranquila"))
				.andExpect(jsonPath("$[0].status").value("DISPONIVEL"))
				.andExpect(jsonPath("$[0].lar_id").value(larId.toString()))
				.andExpect(jsonPath("$[0].criado_em").value("2026-05-18T10:00:00"))
				.andExpect(jsonPath("$[0].atualizado_em").value("2026-05-18T11:00:00"));

		verify(animalService).listarTodos();
	}

	@Test
	void deveRetornarInternalServerError_quandoListagemFalharInesperadamente() throws Exception {
		when(animalService.listarTodos()).thenThrow(new RuntimeException("Banco indisponível"));

		mockMvc.perform(get("/api/v1/animais"))
				.andExpect(status().isInternalServerError())
				.andExpect(jsonPath("$.mensagem").value("Erro interno no servidor"))
				.andExpect(jsonPath("$.erros").isArray());
	}

	@Test
	void deveAtualizarAnimal_quandoDadosValidos() throws Exception {
		UUID id = UUID.fromString("11111111-1111-1111-1111-111111111111");
		String request = """
				{
					"nome": "Mel",
					"especie": "CACHORRO",
					"porte": "MEDIO",
					"idade": 3,
					"descricao": "Brincalhona"
				}
				""";

		mockMvc.perform(put("/api/v1/animais/{id}", id)
						.contentType(MediaType.APPLICATION_JSON)
						.content(request))
				.andExpect(status().isNoContent())
				.andExpect(content().string(""));

		verify(animalService).atualizar(any(), any());
	}

	@Test
	void deveRetornarBadRequest_quandoAtualizacaoInvalida() throws Exception {
		UUID id = UUID.fromString("11111111-1111-1111-1111-111111111111");
		String request = """
				{
					"nome": "",
					"especie": "CACHORRO",
					"porte": "MEDIO",
					"idade": -1,
					"descricao": "Brincalhona"
				}
				""";

		mockMvc.perform(put("/api/v1/animais/{id}", id)
						.contentType(MediaType.APPLICATION_JSON)
						.content(request))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.mensagem").value("Dados inválidos"))
				.andExpect(jsonPath("$.erros").isArray());
	}

	@Test
	void deveRetornarNotFound_quandoAnimalNaoEncontradoParaAtualizacao() throws Exception {
		UUID id = UUID.fromString("11111111-1111-1111-1111-111111111111");
		String request = """
				{
					"nome": "Mel",
					"especie": "CACHORRO",
					"porte": "MEDIO",
					"idade": 3,
					"descricao": "Brincalhona"
				}
				""";
		doThrow(new RecursoNaoEncontradoException("Animal não encontrado."))
				.when(animalService).atualizar(any(), any());

		mockMvc.perform(put("/api/v1/animais/{id}", id)
						.contentType(MediaType.APPLICATION_JSON)
						.content(request))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.mensagem").value("Animal não encontrado."))
				.andExpect(jsonPath("$.erros").isArray());
	}
}
