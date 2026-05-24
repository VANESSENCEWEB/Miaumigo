package com.Miaumigo.Miaumigo.service;

import com.Miaumigo.Miaumigo.domain.Adotante;
import com.Miaumigo.Miaumigo.dto.AdotanteResponse;
import com.Miaumigo.Miaumigo.dto.CadastroAdotanteRequest;
import com.Miaumigo.Miaumigo.exception.CpfJaCadastradoException;
import com.Miaumigo.Miaumigo.exception.EmailJaCadastradoException;
import com.Miaumigo.Miaumigo.repository.AdotanteRepository;
import com.Miaumigo.Miaumigo.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdotanteService {

	private final AdotanteRepository adotanteRepository;
	private final UsuarioRepository usuarioRepository;

	public AdotanteService(AdotanteRepository adotanteRepository, UsuarioRepository usuarioRepository) {
		this.adotanteRepository = adotanteRepository;
		this.usuarioRepository = usuarioRepository;
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
				request.senha(),
				request.cpf(),
				request.preferencias()
		);
		Adotante adotanteSalvo = adotanteRepository.save(adotante);

		return toResponse(adotanteSalvo);
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
}
