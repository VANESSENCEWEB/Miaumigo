package com.Miaumigo.Miaumigo.service;

import com.Miaumigo.Miaumigo.domain.Lar;
import com.Miaumigo.Miaumigo.domain.Operador;
import com.Miaumigo.Miaumigo.dto.CadastroLarRequest;
import com.Miaumigo.Miaumigo.dto.CadastroOperadorRequest;
import com.Miaumigo.Miaumigo.dto.LarResponse;
import com.Miaumigo.Miaumigo.dto.OperadorResponse;
import com.Miaumigo.Miaumigo.exception.CpfJaCadastradoException;
import com.Miaumigo.Miaumigo.exception.EmailJaCadastradoException;
import com.Miaumigo.Miaumigo.exception.RecursoNaoEncontradoException;
import com.Miaumigo.Miaumigo.repository.LarRepository;
import com.Miaumigo.Miaumigo.repository.OperadorRepository;
import com.Miaumigo.Miaumigo.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class LarService {

	private final LarRepository larRepository;
	private final OperadorRepository operadorRepository;
	private final UsuarioRepository usuarioRepository;

	public LarService(
			LarRepository larRepository,
			OperadorRepository operadorRepository,
			UsuarioRepository usuarioRepository
	) {
		this.larRepository = larRepository;
		this.operadorRepository = operadorRepository;
		this.usuarioRepository = usuarioRepository;
	}

	@Transactional
	public LarResponse cadastrar(CadastroLarRequest request) {
		Lar lar = larRepository.save(new Lar(request.nome()));
		return new LarResponse(lar.getId(), lar.getNome());
	}

	@Transactional
	public OperadorResponse cadastrarOperador(UUID larId, CadastroOperadorRequest request) {
		String email = request.email().trim().toLowerCase();
		String cpf = request.cpf().replaceAll("\\D", "");
		if (usuarioRepository.existsByEmail(email)) {
			throw new EmailJaCadastradoException();
		}
		if (usuarioRepository.existsByCpf(cpf)) {
			throw new CpfJaCadastradoException();
		}
		Lar lar = larRepository.findById(larId)
				.orElseThrow(() -> new RecursoNaoEncontradoException("Lar não encontrado."));
		Operador operador = operadorRepository.save(new Operador(
				request.nome(), request.endereco(), request.email(), request.senha(), request.cpf(), lar
		));
		return new OperadorResponse(
				operador.getId(), operador.getNome(), operador.getEmail(), operador.getRole(), operador.getLar().getId()
		);
	}
}
