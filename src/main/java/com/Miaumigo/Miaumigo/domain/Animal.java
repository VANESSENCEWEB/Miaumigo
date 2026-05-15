package com.Miaumigo.Miaumigo.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "animais")
public class Animal {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(nullable = false)
	private String nome;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Especie especie;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Porte porte;

	private Integer idade;

	@Column(length = 1000)
	private String descricao;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private AnimalStatus status;

	@Column(name = "lar_id", nullable = false)
	private UUID larId;

	@Column(name = "criado_em", nullable = false, updatable = false)
	private LocalDateTime criadoEm;

	@Column(name = "atualizado_em", nullable = false)
	private LocalDateTime atualizadoEm;

	protected Animal() {
	}

	public Animal(String nome, Especie especie, Porte porte, Integer idade, String descricao, UUID larId) {
		validarDados(nome, especie, porte, idade, larId);
		this.nome = nome;
		this.especie = especie;
		this.porte = porte;
		this.idade = idade;
		this.descricao = descricao;
		this.larId = larId;
		this.status = AnimalStatus.DISPONIVEL;
		this.criadoEm = LocalDateTime.now();
		this.atualizadoEm = this.criadoEm;
	}

	public UUID getId() {
		return id;
	}

	public String getNome() {
		return nome;
	}

	public void atualizarDados(String nome, Especie especie, Porte porte, Integer idade, String descricao) {
		validarDados(nome, especie, porte, idade, this.larId);
		this.nome = nome;
		this.especie = especie;
		this.porte = porte;
		this.idade = idade;
		this.descricao = descricao;
		this.atualizadoEm = LocalDateTime.now();
	}

	public void transferirParaLar(UUID larId) {
		if (this.status != AnimalStatus.DISPONIVEL) {
			throw new IllegalStateException("Animal só pode ser transferido quando estiver disponível.");
		}
		validarLar(larId);
		this.larId = larId;
		this.atualizadoEm = LocalDateTime.now();
	}

	public void disponibilizar() {
		this.status = AnimalStatus.DISPONIVEL;
		this.atualizadoEm = LocalDateTime.now();
	}

	public void iniciarProcessoAdocao() {
		if (this.status != AnimalStatus.DISPONIVEL) {
			throw new IllegalStateException("Apenas animais disponíveis podem iniciar processo de adoção.");
		}
		this.status = AnimalStatus.EM_PROCESSO;
		this.atualizadoEm = LocalDateTime.now();
	}

	public void marcarComoAdotado() {
		if (this.status != AnimalStatus.EM_PROCESSO) {
			throw new IllegalStateException("Apenas animais em processo de adoção podem ser marcados como adotados.");
		}
		this.status = AnimalStatus.ADOTADO;
		this.atualizadoEm = LocalDateTime.now();
	}

	public Especie getEspecie() {
		return especie;
	}

	public Porte getPorte() {
		return porte;
	}

	public Integer getIdade() {
		return idade;
	}

	public String getDescricao() {
		return descricao;
	}

	public AnimalStatus getStatus() {
		return status;
	}

	public UUID getLarId() {
		return larId;
	}

	public LocalDateTime getCriadoEm() {
		return criadoEm;
	}

	public LocalDateTime getAtualizadoEm() {
		return atualizadoEm;
	}

	private void validarDados(String nome, Especie especie, Porte porte, Integer idade, UUID larId) {
		if (nome == null || nome.isBlank()) {
			throw new IllegalArgumentException("Nome do animal é obrigatório.");
		}
		Objects.requireNonNull(especie, "Espécie do animal é obrigatória.");
		Objects.requireNonNull(porte, "Porte do animal é obrigatório.");
		if (idade != null && idade < 0) {
			throw new IllegalArgumentException("Idade do animal não pode ser negativa.");
		}
		validarLar(larId);
	}

	private void validarLar(UUID larId) {
		if (larId == null) {
			throw new IllegalArgumentException("Lar do animal é obrigatório.");
		}
	}
}
