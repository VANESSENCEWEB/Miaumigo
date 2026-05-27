package com.Miaumigo.Miaumigo.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "solicitacoes_adocao")
public class SolicitacaoAdocao {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "animal_id", nullable = false)
	private Animal animal;

	@ManyToOne(optional = false)
	@JoinColumn(name = "adotante_id", nullable = false)
	private Adotante adotante;

	@ManyToOne
	@JoinColumn(name = "operador_decisao_id")
	private Operador operadorDecisao;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private SolicitacaoStatus status;

	@Column(name = "criado_em", nullable = false, updatable = false)
	private LocalDateTime criadoEm;

	@Column(name = "atualizado_em", nullable = false)
	private LocalDateTime atualizadoEm;

	protected SolicitacaoAdocao() {
	}

	public SolicitacaoAdocao(Animal animal, Adotante adotante) {
		this.animal = Objects.requireNonNull(animal, "Animal é obrigatório.");
		this.adotante = Objects.requireNonNull(adotante, "Adotante é obrigatório.");
		if (animal.getStatus() != AnimalStatus.DISPONIVEL) {
			throw new IllegalStateException("Apenas animais disponíveis aceitam solicitações.");
		}
		this.status = SolicitacaoStatus.PENDENTE;
		this.criadoEm = LocalDateTime.now();
		this.atualizadoEm = this.criadoEm;
	}

	public void aprovar(Operador operador) {
		validarPendente();
		this.operadorDecisao = Objects.requireNonNull(operador, "Operador é obrigatório.");
		this.status = SolicitacaoStatus.APROVADA;
		this.atualizadoEm = LocalDateTime.now();
	}

	public void rejeitar(Operador operador) {
		validarPendente();
		this.operadorDecisao = Objects.requireNonNull(operador, "Operador é obrigatório.");
		this.status = SolicitacaoStatus.REJEITADA;
		this.atualizadoEm = LocalDateTime.now();
	}

	public void cancelar(Adotante adotante) {
		validarPendente();
		if (this.adotante != adotante
				&& (this.adotante.getId() == null || !this.adotante.getId().equals(adotante.getId()))) {
			throw new IllegalArgumentException("Solicitação pertence a outro adotante.");
		}
		this.status = SolicitacaoStatus.CANCELADA;
		this.atualizadoEm = LocalDateTime.now();
	}

	private void validarPendente() {
		if (this.status != SolicitacaoStatus.PENDENTE) {
			throw new IllegalStateException("Apenas solicitações pendentes podem ser alteradas.");
		}
	}

	public UUID getId() {
		return id;
	}

	public Animal getAnimal() {
		return animal;
	}

	public Adotante getAdotante() {
		return adotante;
	}

	public Operador getOperadorDecisao() {
		return operadorDecisao;
	}

	public SolicitacaoStatus getStatus() {
		return status;
	}

	public LocalDateTime getCriadoEm() {
		return criadoEm;
	}

	public LocalDateTime getAtualizadoEm() {
		return atualizadoEm;
	}
}
