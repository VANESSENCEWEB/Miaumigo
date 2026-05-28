package com.Miaumigo.Miaumigo.service;

import com.Miaumigo.Miaumigo.domain.Lar;
import com.Miaumigo.Miaumigo.domain.Operador;
import com.Miaumigo.Miaumigo.domain.Role;
import com.Miaumigo.Miaumigo.dto.CadastroLarComOperadorRequest;
import com.Miaumigo.Miaumigo.dto.CadastroLarComOperadorResponse;
import com.Miaumigo.Miaumigo.dto.CadastroLarRequest;
import com.Miaumigo.Miaumigo.dto.CadastroOperadorRequest;
import com.Miaumigo.Miaumigo.dto.LarResponse;
import com.Miaumigo.Miaumigo.dto.OperadorResponse;
import com.Miaumigo.Miaumigo.repository.LarRepository;
import com.Miaumigo.Miaumigo.repository.OperadorRepository;
import com.Miaumigo.Miaumigo.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LarServiceTest {

	private final LarRepository larRepository = mock(LarRepository.class);
	private final OperadorRepository operadorRepository = mock(OperadorRepository.class);
	private final UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
	private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
	private final LarService service = new LarService(
			larRepository, operadorRepository, usuarioRepository, passwordEncoder
	);

	@Test
	void deveCadastrarLar_quandoNomeValido() {
		UUID id = UUID.randomUUID();
		when(larRepository.save(any())).thenAnswer(invocation -> {
			Lar lar = invocation.getArgument(0);
			ReflectionTestUtils.setField(lar, "id", id);
			return lar;
		});

		LarResponse response = service.cadastrar(new CadastroLarRequest("Lar Amigo"));

		assertEquals(id, response.id());
		assertEquals("Lar Amigo", response.nome());
	}

	@Test
	void deveCadastrarOperador_quandoLarValido() {
		UUID larId = UUID.randomUUID();
		Lar lar = new Lar("Lar Amigo");
		ReflectionTestUtils.setField(lar, "id", larId);
		when(larRepository.findById(larId)).thenReturn(Optional.of(lar));
		when(operadorRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

		OperadorResponse response = service.cadastrarOperador(larId, new CadastroOperadorRequest(
				"Responsavel", "Rua A", "operador@email.com", "senha", "12345678901"
		));

		assertEquals(Role.OPERADOR, response.role());
		assertEquals(larId, response.larId());

		ArgumentCaptor<Operador> operadorCaptor = ArgumentCaptor.forClass(Operador.class);
		verify(operadorRepository).save(operadorCaptor.capture());
		assertNotEquals("senha", operadorCaptor.getValue().getSenha());
		assertTrue(passwordEncoder.matches("senha", operadorCaptor.getValue().getSenha()));
	}

	@Test
	void deveCadastrarLarComOperador_quandoDadosValidos() {
		UUID larId = UUID.randomUUID();
		when(larRepository.save(any())).thenAnswer(invocation -> {
			Lar lar = invocation.getArgument(0);
			ReflectionTestUtils.setField(lar, "id", larId);
			return lar;
		});
		when(operadorRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

		CadastroLarComOperadorResponse response = service.cadastrarComOperador(new CadastroLarComOperadorRequest(
				"Lar Amigo",
				new CadastroOperadorRequest("Responsavel", "Rua A", "operador@email.com", "senha", "12345678901")
		));

		assertEquals(larId, response.lar().id());
		assertEquals("Lar Amigo", response.lar().nome());
		assertEquals(Role.OPERADOR, response.operador().role());
		assertEquals(larId, response.operador().larId());

		ArgumentCaptor<Operador> operadorCaptor = ArgumentCaptor.forClass(Operador.class);
		verify(operadorRepository).save(operadorCaptor.capture());
		assertTrue(passwordEncoder.matches("senha", operadorCaptor.getValue().getSenha()));
	}
}
