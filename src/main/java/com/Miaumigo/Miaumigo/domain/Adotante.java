package com.Miaumigo.Miaumigo.domain;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "adotantes")
public class Adotante extends Usuario {

	@ElementCollection
	@CollectionTable(name = "adotante_preferencias", joinColumns = @JoinColumn(name = "adotante_id"))
	@Enumerated(EnumType.STRING)
	@Column(name = "tag", nullable = false)
	@OrderColumn(name = "ordem")
	private List<Tag> preferencias = new ArrayList<>();

	@ElementCollection
	@CollectionTable(name = "adotante_logs", joinColumns = @JoinColumn(name = "adotante_id"))
	@Column(name = "mensagem", nullable = false, length = 1000)
	@OrderColumn(name = "ordem")
	private List<String> logs = new ArrayList<>();

	protected Adotante() {
	}

	public Adotante(String nome, String endereco, String email, String senha, String cpf, List<Tag> preferencias) {
		super(nome, endereco, email, senha, cpf);
		this.preferencias = normalizarPreferencias(preferencias);
	}

	public List<Tag> getPreferencias() {
		return List.copyOf(preferencias);
	}

	public List<String> getLogs() {
		return List.copyOf(logs);
	}

	public void adicionarLog(String mensagem) {
		String mensagemNormalizada = normalizarTextoOpcional(mensagem);
		if (mensagemNormalizada == null) {
			throw new IllegalArgumentException("Mensagem do log é obrigatória.");
		}
		this.logs.add(mensagemNormalizada);
	}

	private List<Tag> normalizarPreferencias(List<Tag> preferencias) {
		if (preferencias == null) {
			return new ArrayList<>();
		}
		List<Tag> preferenciasNormalizadas = preferencias.stream()
				.filter(Objects::nonNull)
				.distinct()
				.toList();
		return new ArrayList<>(preferenciasNormalizadas);
	}

	private String normalizarTextoOpcional(String texto) {
		if (texto == null || texto.isBlank()) {
			return null;
		}
		return texto.trim();
	}
}
