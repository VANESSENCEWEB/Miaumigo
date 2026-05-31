package com.Miaumigo.Miaumigo.service;

import com.Miaumigo.Miaumigo.domain.Adotante;
import com.Miaumigo.Miaumigo.domain.Especie;
import com.Miaumigo.Miaumigo.domain.ExperienciaAnimais;
import com.Miaumigo.Miaumigo.domain.Porte;
import com.Miaumigo.Miaumigo.domain.Tag;
import com.Miaumigo.Miaumigo.domain.TempoDisponivel;
import com.Miaumigo.Miaumigo.domain.TipoMoradia;
import com.Miaumigo.Miaumigo.dto.AdotanteMeResponse;
import com.Miaumigo.Miaumigo.dto.AdotanteResponse;
import com.Miaumigo.Miaumigo.dto.CadastroAdotanteRequest;
import com.Miaumigo.Miaumigo.dto.PerfilAdotanteRequest;
import com.Miaumigo.Miaumigo.exception.CpfJaCadastradoException;
import com.Miaumigo.Miaumigo.exception.EmailJaCadastradoException;
import com.Miaumigo.Miaumigo.repository.AdotanteRepository;
import com.Miaumigo.Miaumigo.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdotanteServiceTest {

	private final AdotanteRepository adotanteRepository = mock(AdotanteRepository.class);
	private final UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
	private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
	private final AdotanteService adotanteService = new AdotanteService(
			adotanteRepository, usuarioRepository, passwordEncoder
	);

	@Test
	void deveCadastrarAdotante_quandoDadosValidos() {
		UUID id = UUID.randomUUID();
		CadastroAdotanteRequest request = novoRequest(" Maria@Email.com ", "123.456.789-01");
		when(usuarioRepository.existsByEmail("maria@email.com")).thenReturn(false);
		when(usuarioRepository.existsByCpf("12345678901")).thenReturn(false);
		when(adotanteRepository.save(any(Adotante.class))).thenAnswer(invocation -> {
			Adotante adotante = invocation.getArgument(0);
			ReflectionTestUtils.setField(adotante, "id", id);
			return adotante;
		});

		AdotanteResponse response = adotanteService.cadastrar(request);

		ArgumentCaptor<Adotante> adotanteCaptor = ArgumentCaptor.forClass(Adotante.class);
		verify(adotanteRepository).save(adotanteCaptor.capture());
		assertEquals(id, response.id());
		assertEquals("Maria Silva", response.nome());
		assertEquals("maria@email.com", response.email());
		assertEquals("12345678901", response.cpf());
		assertEquals(List.of(Tag.DOCIL, Tag.CARINHOSO), response.preferencias());
		assertEquals(List.of(Tag.DOCIL, Tag.CARINHOSO), adotanteCaptor.getValue().getPreferencias());
		assertNotEquals("senha123", adotanteCaptor.getValue().getSenha());
		assertTrue(passwordEncoder.matches("senha123", adotanteCaptor.getValue().getSenha()));
	}

	@Test
	void deveLancarExcecao_quandoEmailJaCadastrado() {
		CadastroAdotanteRequest request = novoRequest(" Maria@Email.com ", "12345678901");
		when(usuarioRepository.existsByEmail("maria@email.com")).thenReturn(true);

		assertThrows(EmailJaCadastradoException.class, () -> adotanteService.cadastrar(request));
	}

	@Test
	void deveLancarExcecao_quandoCpfJaCadastrado() {
		CadastroAdotanteRequest request = novoRequest("maria@email.com", "123.456.789-01");
		when(usuarioRepository.existsByEmail("maria@email.com")).thenReturn(false);
		when(usuarioRepository.existsByCpf("12345678901")).thenReturn(true);

		assertThrows(CpfJaCadastradoException.class, () -> adotanteService.cadastrar(request));
	}

	@Test
	void deveAtualizarPerfilProgressivo_quandoPayloadValido() {
		UUID adotanteId = UUID.randomUUID();
		Adotante adotante = new Adotante("Maria Silva", "Rua das Flores", "maria@email.com", "senha", "12345678901", List.of());
		ReflectionTestUtils.setField(adotante, "id", adotanteId);
		when(adotanteRepository.findById(adotanteId)).thenReturn(Optional.of(adotante));

		AdotanteMeResponse response = adotanteService.atualizarPerfil(adotanteId, perfilCompletoRequest());

		assertEquals(List.of(Especie.GATO), response.especiesPreferidas());
		assertEquals(List.of(Tag.CALMO, Tag.CARINHOSO), response.preferencias());
		assertEquals(TipoMoradia.APARTAMENTO, response.tipoMoradia());
		assertEquals(Porte.PEQUENO, response.espacoDisponivel());
		assertEquals(TempoDisponivel.UMA_HORA, response.tempoDisponivel());
		assertEquals(ExperienciaAnimais.JA_TIVE_PETS, response.experienciaAnimais());
		assertTrue(response.perfilCompleto());
	}

	private CadastroAdotanteRequest novoRequest(String email, String cpf) {
		return new CadastroAdotanteRequest(
				"Maria Silva",
				"Rua das Flores, 123",
				email,
				"senha123",
				cpf,
				List.of(Tag.DOCIL, Tag.CARINHOSO),
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null
		);
	}

	private PerfilAdotanteRequest perfilCompletoRequest() {
		return new PerfilAdotanteRequest(
				List.of(Especie.GATO),
				List.of(Tag.CALMO, Tag.CARINHOSO),
				TipoMoradia.APARTAMENTO,
				Porte.PEQUENO,
				TempoDisponivel.UMA_HORA,
				ExperienciaAnimais.JA_TIVE_PETS,
				false,
				false,
				true,
				"81999999999",
				"Recife"
		);
	}
}
