package com.Miaumigo.Miaumigo.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.util.UUID;

@Entity
@Table(
		name = "usuarios",
		uniqueConstraints = {
				@UniqueConstraint(name = "uk_usuarios_email", columnNames = "email"),
				@UniqueConstraint(name = "uk_usuarios_cpf", columnNames = "cpf")
		}
)
@Inheritance(strategy = InheritanceType.JOINED)
public class Usuario {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(nullable = false)
	private String nome;

	@Column(nullable = false)
	private String endereco;

	@Column(nullable = false)
	private String email;

	@Column(nullable = false)
	private String senha;

	@Column(nullable = false, length = 11)
	private String cpf;

	protected Usuario() {
	}

	public Usuario(String nome, String endereco, String email, String senha, String cpf) {
		this.nome = normalizarTextoObrigatorio(nome, "Nome do usuário é obrigatório.");
		this.endereco = normalizarTextoObrigatorio(endereco, "Endereço do usuário é obrigatório.");
		this.email = normalizarEmail(email);
		this.senha = normalizarTextoObrigatorio(senha, "Senha do usuário é obrigatória.");
		this.cpf = normalizarCpf(cpf);
	}

	public UUID getId() {
		return id;
	}

	public String getNome() {
		return nome;
	}

	public String getEndereco() {
		return endereco;
	}

	public String getEmail() {
		return email;
	}

	public String getSenha() {
		return senha;
	}

	public String getCpf() {
		return cpf;
	}

	private String normalizarEmail(String email) {
		return normalizarTextoObrigatorio(email, "Email do usuário é obrigatório.").toLowerCase();
	}

	private String normalizarCpf(String cpf) {
		String cpfNormalizado = normalizarTextoObrigatorio(cpf, "CPF do usuário é obrigatório.")
				.replaceAll("\\D", "");
		if (cpfNormalizado.length() != 11) {
			throw new IllegalArgumentException("CPF do usuário deve conter 11 dígitos.");
		}
		return cpfNormalizado;
	}

	private String normalizarTextoObrigatorio(String texto, String mensagem) {
		if (texto == null || texto.isBlank()) {
			throw new IllegalArgumentException(mensagem);
		}
		return texto.trim();
	}
}
