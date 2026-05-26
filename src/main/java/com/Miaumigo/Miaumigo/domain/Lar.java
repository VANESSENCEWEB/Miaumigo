package com.Miaumigo.Miaumigo.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "lares")
public class Lar {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(nullable = false)
	private String nome;

	protected Lar() {
	}

	public Lar(String nome) {
		if (nome == null || nome.isBlank()) {
			throw new IllegalArgumentException("Nome do lar é obrigatório.");
		}
		this.nome = nome.trim();
	}

	public UUID getId() {
		return id;
	}

	public String getNome() {
		return nome;
	}
}
