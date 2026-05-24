package com.Miaumigo.Miaumigo.exception;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ApiExceptionHandlerTest {

	private final MockMvc mockMvc = MockMvcBuilders
			.standaloneSetup(new TestController())
			.setControllerAdvice(new ApiExceptionHandler())
			.build();

	@Test
	void deveRetornarBadRequest_quandoEmailJaCadastrado() throws Exception {
		mockMvc.perform(get("/teste/email-duplicado"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.mensagem").value("Email já cadastrado."))
				.andExpect(jsonPath("$.erros").isArray());
	}

	@Test
	void deveRetornarBadRequest_quandoCpfJaCadastrado() throws Exception {
		mockMvc.perform(get("/teste/cpf-duplicado"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.mensagem").value("CPF já cadastrado."))
				.andExpect(jsonPath("$.erros").isArray());
	}

	@Test
	void deveRetornarBadRequest_quandoBancoIndicarEmailDuplicado() throws Exception {
		mockMvc.perform(get("/teste/integridade-email"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.mensagem").value("Email já cadastrado."))
				.andExpect(jsonPath("$.erros").isArray());
	}

	@Test
	void deveRetornarBadRequest_quandoBancoIndicarCpfDuplicado() throws Exception {
		mockMvc.perform(get("/teste/integridade-cpf"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.mensagem").value("CPF já cadastrado."))
				.andExpect(jsonPath("$.erros").isArray());
	}

	@RestController
	static class TestController {

		@GetMapping("/teste/email-duplicado")
		void emailDuplicado() {
			throw new EmailJaCadastradoException();
		}

		@GetMapping("/teste/cpf-duplicado")
		void cpfDuplicado() {
			throw new CpfJaCadastradoException();
		}

		@GetMapping("/teste/integridade-email")
		void integridadeEmail() {
			throw new DataIntegrityViolationException(
					"Erro de integridade",
					new RuntimeException("duplicate key value violates unique constraint \"uk_usuarios_email\"")
			);
		}

		@GetMapping("/teste/integridade-cpf")
		void integridadeCpf() {
			throw new DataIntegrityViolationException(
					"Erro de integridade",
					new RuntimeException("duplicate key value violates unique constraint \"uk_usuarios_cpf\"")
			);
		}
	}
}
