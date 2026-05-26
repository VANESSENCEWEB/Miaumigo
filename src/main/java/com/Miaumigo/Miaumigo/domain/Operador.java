package com.Miaumigo.Miaumigo.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "operadores")
public class Operador extends Usuario {

	@ManyToOne(optional = false)
	@JoinColumn(name = "lar_id", nullable = false)
	private Lar lar;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Role role;

	protected Operador() {
	}

	public Operador(String nome, String endereco, String email, String senha, String cpf, Lar lar) {
		super(nome, endereco, email, senha, cpf);
		if (lar == null) {
			throw new IllegalArgumentException("Lar do operador é obrigatório.");
		}
		this.lar = lar;
		this.role = Role.OPERADOR;
	}

	public Lar getLar() {
		return lar;
	}

	public Role getRole() {
		return role;
	}
}
