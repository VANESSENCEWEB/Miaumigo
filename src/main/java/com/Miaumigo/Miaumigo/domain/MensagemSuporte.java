package com.Miaumigo.Miaumigo.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "mensagens_suporte")
public class MensagemSuporte {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "adotante_id", nullable = false)
	private Adotante adotante;

	@Column(nullable = false, length = 120)
	private String assunto;

	@Column(nullable = false, length = 1000)
	private String mensagem;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private MensagemSuporteStatus status;

	@Column(name = "criado_em", nullable = false, updatable = false)
	private LocalDateTime criadoEm;

	protected MensagemSuporte() {
	}

	public MensagemSuporte(Adotante adotante, String assunto, String mensagem) {
		if (adotante == null) {
			throw new IllegalArgumentException("Adotante é obrigatório.");
		}
		this.adotante = adotante;
		this.assunto = normalizarAssunto(assunto);
		this.mensagem = normalizarMensagem(mensagem);
		this.status = MensagemSuporteStatus.NOVA;
	}

	@PrePersist
	void prePersist() {
		if (criadoEm == null) {
			criadoEm = LocalDateTime.now();
		}
	}

	public UUID getId() {
		return id;
	}

	public Adotante getAdotante() {
		return adotante;
	}

	public String getAssunto() {
		return assunto;
	}

	public String getMensagem() {
		return mensagem;
	}

	public MensagemSuporteStatus getStatus() {
		return status;
	}

	public LocalDateTime getCriadoEm() {
		return criadoEm;
	}

	private String normalizarAssunto(String valor) {
		if (valor == null || valor.isBlank()) {
			throw new IllegalArgumentException("Assunto é obrigatório.");
		}
		String normalizado = valor.trim();
		if (normalizado.length() > 120) {
			throw new IllegalArgumentException("Assunto deve ter no máximo 120 caracteres.");
		}
		return normalizado;
	}

	private String normalizarMensagem(String valor) {
		if (valor == null || valor.isBlank()) {
			throw new IllegalArgumentException("Mensagem é obrigatória.");
		}
		String normalizado = valor.trim();
		if (normalizado.length() < 10 || normalizado.length() > 1000) {
			throw new IllegalArgumentException("Mensagem deve ter entre 10 e 1000 caracteres.");
		}
		return normalizado;
	}
}
