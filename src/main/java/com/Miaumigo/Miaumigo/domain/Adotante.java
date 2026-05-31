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
	@CollectionTable(name = "adotante_especies_preferidas", joinColumns = @JoinColumn(name = "adotante_id"))
	@Enumerated(EnumType.STRING)
	@Column(name = "especie", nullable = false)
	@OrderColumn(name = "ordem")
	private List<Especie> especiesPreferidas = new ArrayList<>();

	@Enumerated(EnumType.STRING)
	@Column(name = "tipo_moradia")
	private TipoMoradia tipoMoradia;

	@Enumerated(EnumType.STRING)
	@Column(name = "espaco_disponivel")
	private Porte espacoDisponivel;

	@Enumerated(EnumType.STRING)
	@Column(name = "tempo_disponivel")
	private TempoDisponivel tempoDisponivel;

	@Enumerated(EnumType.STRING)
	@Column(name = "experiencia_animais")
	private ExperienciaAnimais experienciaAnimais;

	@Column(name = "possui_criancas")
	private Boolean possuiCriancas;

	@Column(name = "possui_caes")
	private Boolean possuiCaes;

	@Column(name = "possui_gatos")
	private Boolean possuiGatos;

	@Column(name = "telefone")
	private String telefone;

	@Column(name = "cidade")
	private String cidade;

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

	public List<Especie> getEspeciesPreferidas() {
		return List.copyOf(especiesPreferidas);
	}

	public TipoMoradia getTipoMoradia() {
		return tipoMoradia;
	}

	public Porte getEspacoDisponivel() {
		return espacoDisponivel;
	}

	public TempoDisponivel getTempoDisponivel() {
		return tempoDisponivel;
	}

	public ExperienciaAnimais getExperienciaAnimais() {
		return experienciaAnimais;
	}

	public Boolean getPossuiCriancas() {
		return possuiCriancas;
	}

	public Boolean getPossuiCaes() {
		return possuiCaes;
	}

	public Boolean getPossuiGatos() {
		return possuiGatos;
	}

	public String getTelefone() {
		return telefone;
	}

	public String getCidade() {
		return cidade;
	}

	public boolean isPerfilCompleto() {
		return !especiesPreferidas.isEmpty()
				&& !preferencias.isEmpty()
				&& tipoMoradia != null
				&& espacoDisponivel != null
				&& tempoDisponivel != null
				&& experienciaAnimais != null
				&& possuiCriancas != null
				&& possuiCaes != null
				&& possuiGatos != null;
	}

	public void atualizarPerfil(
			List<Especie> especiesPreferidas,
			List<Tag> preferencias,
			TipoMoradia tipoMoradia,
			Porte espacoDisponivel,
			TempoDisponivel tempoDisponivel,
			ExperienciaAnimais experienciaAnimais,
			Boolean possuiCriancas,
			Boolean possuiCaes,
			Boolean possuiGatos,
			String telefone,
			String cidade
	) {
		this.especiesPreferidas = normalizarLista(especiesPreferidas);
		this.preferencias = normalizarPreferencias(preferencias);
		this.tipoMoradia = tipoMoradia;
		this.espacoDisponivel = espacoDisponivel;
		this.tempoDisponivel = tempoDisponivel;
		this.experienciaAnimais = experienciaAnimais;
		this.possuiCriancas = possuiCriancas;
		this.possuiCaes = possuiCaes;
		this.possuiGatos = possuiGatos;
		this.telefone = normalizarTextoOpcional(telefone);
		this.cidade = normalizarTextoOpcional(cidade);
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
		return normalizarLista(preferencias);
	}

	private <T> List<T> normalizarLista(List<T> valores) {
		if (valores == null) {
			return new ArrayList<>();
		}
		List<T> valoresNormalizados = valores.stream()
				.filter(Objects::nonNull)
				.distinct()
				.toList();
		return new ArrayList<>(valoresNormalizados);
	}

	private String normalizarTextoOpcional(String texto) {
		if (texto == null || texto.isBlank()) {
			return null;
		}
		return texto.trim();
	}
}
