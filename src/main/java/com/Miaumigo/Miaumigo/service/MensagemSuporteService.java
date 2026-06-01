package com.Miaumigo.Miaumigo.service;

import com.Miaumigo.Miaumigo.domain.Adotante;
import com.Miaumigo.Miaumigo.domain.MensagemSuporte;
import com.Miaumigo.Miaumigo.dto.MensagemSuporteRequest;
import com.Miaumigo.Miaumigo.dto.MensagemSuporteResponse;
import com.Miaumigo.Miaumigo.exception.IdentidadeNaoAutenticadaException;
import com.Miaumigo.Miaumigo.repository.AdotanteRepository;
import com.Miaumigo.Miaumigo.repository.MensagemSuporteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class MensagemSuporteService {

	private final MensagemSuporteRepository mensagemSuporteRepository;
	private final AdotanteRepository adotanteRepository;

	public MensagemSuporteService(
			MensagemSuporteRepository mensagemSuporteRepository,
			AdotanteRepository adotanteRepository
	) {
		this.mensagemSuporteRepository = mensagemSuporteRepository;
		this.adotanteRepository = adotanteRepository;
	}

	@Transactional
	public MensagemSuporteResponse criar(UUID adotanteId, MensagemSuporteRequest request) {
		Adotante adotante = adotanteRepository.findById(adotanteId)
				.orElseThrow(() -> new IdentidadeNaoAutenticadaException("Adotante não autenticado."));
		MensagemSuporte mensagem = new MensagemSuporte(adotante, request.assunto(), request.mensagem());
		return toResponse(mensagemSuporteRepository.save(mensagem));
	}

	private MensagemSuporteResponse toResponse(MensagemSuporte mensagem) {
		return new MensagemSuporteResponse(
				mensagem.getId(),
				mensagem.getAssunto(),
				mensagem.getMensagem(),
				mensagem.getStatus(),
				mensagem.getCriadoEm()
		);
	}
}
