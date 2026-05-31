package com.Miaumigo.Miaumigo.service;

import com.Miaumigo.Miaumigo.domain.Adotante;
import com.Miaumigo.Miaumigo.dto.AdotanteMeResponse;
import com.Miaumigo.Miaumigo.dto.AdotanteResponse;
import com.Miaumigo.Miaumigo.dto.CadastroAdotanteRequest;
import com.Miaumigo.Miaumigo.dto.PerfilAdotanteRequest;
import com.Miaumigo.Miaumigo.exception.CpfJaCadastradoException;
import com.Miaumigo.Miaumigo.exception.EmailJaCadastradoException;
import com.Miaumigo.Miaumigo.exception.RecursoNaoEncontradoException;
import com.Miaumigo.Miaumigo.repository.AdotanteRepository;
import com.Miaumigo.Miaumigo.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdotanteService {

	private final AdotanteRepository adotanteRepository;
	private final UsuarioRepository usuarioRepository;
	private final PasswordEncoder passwordEncoder;

	public AdotanteService(
			AdotanteRepository adotanteRepository,
			UsuarioRepository usuarioRepository,
			PasswordEncoder passwordEncoder
	) {
		this.adotanteRepository = adotanteRepository;
		this.usuarioRepository = usuarioRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Transactional
	public AdotanteResponse cadastrar(CadastroAdotanteRequest request) {
		if (usuarioRepository.existsByEmail(request.email().trim().toLowerCase())) {
			throw new EmailJaCadastradoException();
		}
		String cpfNormalizado = request.cpf().replaceAll("\\D", "");
		if (usuarioRepository.existsByCpf(cpfNormalizado)) {
			throw new CpfJaCadastradoException();
		}

		Adotante adotante = new Adotante(
				request.nome(),
				request.endereco(),
				request.email(),
				passwordEncoder.encode(request.senha()),
				request.cpf(),
				request.preferencias()
		);
		adotante.atualizarPerfil(
				request.especiesPreferidas(),
				request.preferencias(),
				request.tipoMoradia(),
				request.espacoDisponivel(),
				request.tempoDisponivel(),
				request.experienciaAnimais(),
				request.possuiCriancas(),
				request.possuiCaes(),
				request.possuiGatos(),
				request.telefone(),
				request.cidade()
		);
		Adotante adotanteSalvo = adotanteRepository.save(adotante);

		return toResponse(adotanteSalvo);
	}

	@Transactional(readOnly = true)
	public AdotanteMeResponse buscarMeuPerfil(java.util.UUID adotanteId) {
		Adotante adotante = buscarAdotante(adotanteId);
		return toMeResponse(adotante);
	}

	@Transactional
	public AdotanteMeResponse atualizarPerfil(java.util.UUID adotanteId, PerfilAdotanteRequest request) {
		Adotante adotante = buscarAdotante(adotanteId);
		adotante.atualizarPerfil(
				request.especiesPreferidas(),
				request.preferencias(),
				request.tipoMoradia(),
				request.espacoDisponivel(),
				request.tempoDisponivel(),
				request.experienciaAnimais(),
				request.possuiCriancas(),
				request.possuiCaes(),
				request.possuiGatos(),
				request.telefone(),
				request.cidade()
		);
		return toMeResponse(adotante);
	}

	private Adotante buscarAdotante(java.util.UUID adotanteId) {
		return adotanteRepository.findById(adotanteId)
				.orElseThrow(() -> new RecursoNaoEncontradoException("Adotante não encontrado."));
	}

	private AdotanteResponse toResponse(Adotante adotante) {
		return new AdotanteResponse(
				adotante.getId(),
				adotante.getNome(),
				adotante.getEndereco(),
				adotante.getEmail(),
				adotante.getCpf(),
				adotante.getPreferencias()
		);
	}

	private AdotanteMeResponse toMeResponse(Adotante adotante) {
		return new AdotanteMeResponse(
				adotante.getId(),
				adotante.getNome(),
				adotante.getEndereco(),
				adotante.getEmail(),
				adotante.getCpf(),
				adotante.getPreferencias(),
				adotante.getEspeciesPreferidas(),
				adotante.getTipoMoradia(),
				adotante.getEspacoDisponivel(),
				adotante.getTempoDisponivel(),
				adotante.getExperienciaAnimais(),
				adotante.getPossuiCriancas(),
				adotante.getPossuiCaes(),
				adotante.getPossuiGatos(),
				adotante.getTelefone(),
				adotante.getCidade(),
				adotante.isPerfilCompleto()
		);
	}
}
