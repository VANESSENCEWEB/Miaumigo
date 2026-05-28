package com.Miaumigo.Miaumigo.controller;

import com.Miaumigo.Miaumigo.domain.Especie;
import com.Miaumigo.Miaumigo.domain.AnimalStatus;
import com.Miaumigo.Miaumigo.domain.Porte;
import com.Miaumigo.Miaumigo.domain.Tag;
import com.Miaumigo.Miaumigo.dto.AnimalResponse;
import com.Miaumigo.Miaumigo.dto.SolicitacaoAdocaoResponse;
import com.Miaumigo.Miaumigo.dto.TextoDivulgacaoResponse;
import com.Miaumigo.Miaumigo.exception.IdentidadeNaoAutenticadaException;
import com.Miaumigo.Miaumigo.exception.IntegracaoGeminiException;
import com.Miaumigo.Miaumigo.exception.RecursoNaoEncontradoException;
import com.Miaumigo.Miaumigo.security.UsuarioAutenticadoService;
import com.Miaumigo.Miaumigo.service.AnimalService;
import com.Miaumigo.Miaumigo.service.SolicitacaoAdocaoService;
import com.Miaumigo.Miaumigo.service.TextoDivulgacaoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;
import java.util.List;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AnimalController.class)
@AutoConfigureMockMvc(addFilters = false)
class AnimalControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private AnimalService animalService;

	@MockitoBean
	private SolicitacaoAdocaoService solicitacaoService;

	@MockitoBean
	private TextoDivulgacaoService textoDivulgacaoService;

	@MockitoBean
	private UsuarioAutenticadoService usuarioAutenticadoService;

	@BeforeEach
	void configurarIdentidade() {
		when(usuarioAutenticadoService.exigirOperador(any())).thenReturn(UUID.randomUUID());
		when(usuarioAutenticadoService.exigirOperadorOuAdmin(any())).thenReturn(UUID.randomUUID());
	}

	@Test
	void deveCadastrarAnimal_quandoDadosValidos() throws Exception {
		String request = """
				{
					"nome": "Luna",
					"especie": "GATO",
					"porte": "PEQUENO",
					"idade": 2,
					"descricao": "Dócil e tranquila",
					"tags": ["DOCIL", "CARINHOSO"],
					"cloudinary_public_id": "animais/luna"
				}
		""";

		mockMvc.perform(post("/api/v1/animais")
						.contentType(MediaType.APPLICATION_JSON)
						.content(request))
				.andExpect(status().isCreated())
				.andExpect(content().string(""));

		verify(animalService).cadastrar(any(), any());
	}

	@Test
	void deveCadastrarAnimal_quandoTagsDeMatchmakingInformadas() throws Exception {
		String request = """
				{
					"nome": "Thor",
					"especie": "CACHORRO",
					"porte": "MEDIO",
					"idade": 3,
					"tags": [
						"BRINCALHAO",
						"CALMO",
						"INDEPENDENTE",
						"CARINHOSO",
						"SOCIAL",
						"PROTETOR",
						"ENERGICO",
						"ADAPTADO_A_APARTAMENTO",
						"PRECISA_DE_ESPACO",
						"CONVIVE_COM_CRIANCAS",
						"CONVIVE_COM_CAES",
						"CONVIVE_COM_GATOS"
					]
				}
				""";

		mockMvc.perform(post("/api/v1/animais")
						.contentType(MediaType.APPLICATION_JSON)
						.content(request))
				.andExpect(status().isCreated());

		verify(animalService).cadastrar(argThat(cadastro ->
					cadastro.tags().equals(List.of(
						Tag.BRINCALHAO,
						Tag.CALMO,
						Tag.INDEPENDENTE,
						Tag.CARINHOSO,
						Tag.SOCIAL,
						Tag.PROTETOR,
						Tag.ENERGICO,
						Tag.ADAPTADO_A_APARTAMENTO,
						Tag.PRECISA_DE_ESPACO,
						Tag.CONVIVE_COM_CRIANCAS,
						Tag.CONVIVE_COM_CAES,
						Tag.CONVIVE_COM_GATOS
					))), any());
	}

	@Test
	void deveRetornarBadRequest_quandoRequestInvalida() throws Exception {
		String request = """
				{
					"nome": "",
					"especie": "GATO",
					"porte": "PEQUENO",
					"idade": -1
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
					"tags": ["DOCIL"],
					"cloudinary_public_id": "animais/luna"
				}
				""";
			doThrow(new IllegalArgumentException("Nome do animal é obrigatório."))
					.when(animalService).cadastrar(any(), any());

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
					"tags": ["DOCIL"],
					"cloudinary_public_id": "animais/luna"
				}
				""";
			doThrow(new RuntimeException("Banco indisponível"))
					.when(animalService).cadastrar(any(), any());

		mockMvc.perform(post("/api/v1/animais")
						.contentType(MediaType.APPLICATION_JSON)
						.content(request))
				.andExpect(status().isInternalServerError())
				.andExpect(jsonPath("$.mensagem").value("Erro interno no servidor"))
				.andExpect(jsonPath("$.erros").isArray());
	}

	@Test
	void deveSolicitarAdocao_quandoAdotanteInformado() throws Exception {
		UUID id = UUID.fromString("11111111-1111-1111-1111-111111111111");
		UUID adotanteId = UUID.fromString("22222222-2222-2222-2222-222222222222");
		when(usuarioAutenticadoService.exigirAdotante(any())).thenReturn(adotanteId);
		when(solicitacaoService.criar(id, adotanteId)).thenReturn(new SolicitacaoAdocaoResponse(
				UUID.randomUUID(), id, "Luna", adotanteId, "Maria", com.Miaumigo.Miaumigo.domain.SolicitacaoStatus.PENDENTE,
				LocalDateTime.now(), LocalDateTime.now()
		));

		mockMvc.perform(post("/api/v1/animais/{id}/solicitacoes", id)
						.header("Authorization", "Bearer token"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.status").value("PENDENTE"));
	}

	@Test
	void deveRetornarUnauthorized_quandoSolicitacaoSemIdentidade() throws Exception {
		doThrow(new IdentidadeNaoAutenticadaException("Usuário não autenticado."))
				.when(usuarioAutenticadoService).exigirAdotante(any());

		mockMvc.perform(post("/api/v1/animais/{id}/solicitacoes", UUID.randomUUID()))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.mensagem").value("Usuário não autenticado."));
	}

	@Test
	void deveRetornarNotFound_quandoAnimalNaoEncontradoParaSolicitacao() throws Exception {
		UUID id = UUID.fromString("11111111-1111-1111-1111-111111111111");
		UUID adotanteId = UUID.fromString("22222222-2222-2222-2222-222222222222");
		when(usuarioAutenticadoService.exigirAdotante(any())).thenReturn(adotanteId);
		doThrow(new RecursoNaoEncontradoException("Animal não encontrado."))
				.when(solicitacaoService).criar(any(), any());

		mockMvc.perform(post("/api/v1/animais/{id}/solicitacoes", id)
						.header("Authorization", "Bearer token"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.mensagem").value("Animal não encontrado."))
				.andExpect(jsonPath("$.erros").isArray());
	}

	@Test
	void deveRetornarAnimal_quandoIdValido() throws Exception {
		UUID id = UUID.fromString("11111111-1111-1111-1111-111111111111");
		when(animalService.buscarPorId(id))
				.thenReturn(new AnimalResponse(
						id,
						"Luna",
						2,
							Porte.PEQUENO,
							Especie.GATO,
							"Dócil e tranquila",
							AnimalStatus.DISPONIVEL,
							List.of(Tag.DOCIL, Tag.CARINHOSO),
						"animais/luna"
				));

		mockMvc.perform(get("/api/v1/animais/{id}", id))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(id.toString()))
				.andExpect(jsonPath("$.nome").value("Luna"))
				.andExpect(jsonPath("$.idade").value(2))
				.andExpect(jsonPath("$.porte").value("PEQUENO"))
					.andExpect(jsonPath("$.especie").value("GATO"))
					.andExpect(jsonPath("$.descricao").value("Dócil e tranquila"))
					.andExpect(jsonPath("$.status").value("DISPONIVEL"))
				.andExpect(jsonPath("$.tags[0]").value("DOCIL"))
				.andExpect(jsonPath("$.tags[1]").value("CARINHOSO"))
				.andExpect(jsonPath("$.cloudinary_public_id").value("animais/luna"));

		verify(animalService).buscarPorId(id);
	}

	@Test
	void deveRetornarNotFound_quandoAnimalNaoEncontrado() throws Exception {
		UUID id = UUID.fromString("11111111-1111-1111-1111-111111111111");
		doThrow(new RecursoNaoEncontradoException("Animal não encontrado"))
				.when(animalService).buscarPorId(id);

		mockMvc.perform(get("/api/v1/animais/{id}", id))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.mensagem").value("Animal não encontrado"))
				.andExpect(jsonPath("$.erros").isArray());
	}

	@Test
	void deveGerarTextoDivulgacao_quandoAnimalDisponivel() throws Exception {
		UUID id = UUID.fromString("11111111-1111-1111-1111-111111111111");
		when(textoDivulgacaoService.gerar(id))
				.thenReturn(new TextoDivulgacaoResponse("Luna está esperando por uma família! #Adote"));

		mockMvc.perform(post("/api/v1/animais/{id}/texto-divulgacao", id))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.texto").value("Luna está esperando por uma família! #Adote"));

		verify(textoDivulgacaoService).gerar(id);
	}

	@Test
	void deveRetornarBadGateway_quandoGeminiFalhar() throws Exception {
		UUID id = UUID.fromString("11111111-1111-1111-1111-111111111111");
		doThrow(new IntegracaoGeminiException("Não foi possível gerar o texto de divulgação."))
				.when(textoDivulgacaoService).gerar(id);

		mockMvc.perform(post("/api/v1/animais/{id}/texto-divulgacao", id))
				.andExpect(status().isBadGateway())
				.andExpect(jsonPath("$.mensagem").value("Não foi possível gerar o texto de divulgação."))
				.andExpect(jsonPath("$.erros").isArray());
	}

	@Test
	void deveRetornarBadRequest_quandoAnimalNaoEstiverDisponivelParaDivulgacao() throws Exception {
		UUID id = UUID.fromString("11111111-1111-1111-1111-111111111111");
		doThrow(new IllegalStateException("Somente animais disponíveis podem ter texto de divulgação gerado."))
				.when(textoDivulgacaoService).gerar(id);

		mockMvc.perform(post("/api/v1/animais/{id}/texto-divulgacao", id))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.mensagem")
						.value("Somente animais disponíveis podem ter texto de divulgação gerado."))
				.andExpect(jsonPath("$.erros").isArray());
	}

	@Test
	void deveRetornarNotFound_quandoAnimalDaDivulgacaoNaoExistir() throws Exception {
		UUID id = UUID.fromString("11111111-1111-1111-1111-111111111111");
		doThrow(new RecursoNaoEncontradoException("Animal não encontrado."))
				.when(textoDivulgacaoService).gerar(id);

		mockMvc.perform(post("/api/v1/animais/{id}/texto-divulgacao", id))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.mensagem").value("Animal não encontrado."))
				.andExpect(jsonPath("$.erros").isArray());
	}
}
