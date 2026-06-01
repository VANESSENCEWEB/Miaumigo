package com.Miaumigo.Miaumigo.service;

import com.Miaumigo.Miaumigo.domain.Adotante;
import com.Miaumigo.Miaumigo.domain.MensagemSuporte;
import com.Miaumigo.Miaumigo.domain.MensagemSuporteStatus;
import com.Miaumigo.Miaumigo.dto.MensagemSuporteRequest;
import com.Miaumigo.Miaumigo.dto.MensagemSuporteResponse;
import com.Miaumigo.Miaumigo.exception.IdentidadeNaoAutenticadaException;
import com.Miaumigo.Miaumigo.repository.AdotanteRepository;
import com.Miaumigo.Miaumigo.repository.MensagemSuporteRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MensagemSuporteServiceTest {

	private final MensagemSuporteRepository mensagemSuporteRepository = mock(MensagemSuporteRepository.class);
	private final AdotanteRepository adotanteRepository = mock(AdotanteRepository.class);
	private final MensagemSuporteService service = new MensagemSuporteService(
			mensagemSuporteRepository, adotanteRepository
	);

	@Test
	void deveCriarMensagemSuporte_quandoAdotanteAutenticadoEnviarDadosValidos() {
		UUID adotanteId = UUID.randomUUID();
		UUID mensagemId = UUID.randomUUID();
		LocalDateTime criadoEm = LocalDateTime.of(2026, 6, 1, 10, 30);
		Adotante adotante = novoAdotante();
		when(adotanteRepository.findById(adotanteId)).thenReturn(Optional.of(adotante));
		when(mensagemSuporteRepository.save(any(MensagemSuporte.class))).thenAnswer(invocation -> {
			MensagemSuporte mensagem = invocation.getArgument(0);
			ReflectionTestUtils.setField(mensagem, "id", mensagemId);
			ReflectionTestUtils.setField(mensagem, "criadoEm", criadoEm);
			return mensagem;
		});

		MensagemSuporteResponse response = service.criar(
				adotanteId,
				new MensagemSuporteRequest(" Dúvida sobre adoção ", " Gostaria de entender o processo. ")
		);

		ArgumentCaptor<MensagemSuporte> mensagemCaptor = ArgumentCaptor.forClass(MensagemSuporte.class);
		verify(mensagemSuporteRepository).save(mensagemCaptor.capture());
		assertEquals(mensagemId, response.id());
		assertEquals("Dúvida sobre adoção", response.assunto());
		assertEquals("Gostaria de entender o processo.", response.mensagem());
		assertEquals(MensagemSuporteStatus.NOVA, response.status());
		assertEquals(criadoEm, response.criadoEm());
		assertEquals(adotante, mensagemCaptor.getValue().getAdotante());
	}

	@Test
	void deveLancarExcecao_quandoMensagemForCurta() {
		UUID adotanteId = UUID.randomUUID();
		when(adotanteRepository.findById(adotanteId)).thenReturn(Optional.of(novoAdotante()));

		assertThrows(IllegalArgumentException.class, () -> service.criar(
				adotanteId,
				new MensagemSuporteRequest("Dúvida", "curta")
		));
	}

	@Test
	void deveLancarExcecao_quandoAdotanteNaoForEncontrado() {
		UUID adotanteId = UUID.randomUUID();
		when(adotanteRepository.findById(adotanteId)).thenReturn(Optional.empty());

		assertThrows(IdentidadeNaoAutenticadaException.class, () -> service.criar(
				adotanteId,
				new MensagemSuporteRequest("Dúvida", "Mensagem com tamanho válido.")
		));
	}

	private Adotante novoAdotante() {
		return new Adotante(
				"Maria Silva",
				"Rua das Flores",
				"maria@email.com",
				"senha",
				"12345678901",
				List.of()
		);
	}
}
