package com.Miaumigo.Miaumigo.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
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

	@Enumerated(EnumType.STRING)
	@Column(name = "sexo")
	private SexoAnimal sexo;

	private Integer idade;

	@Column(length = 1000)
	private String descricao;

	@ElementCollection
	@CollectionTable(name = "animal_tags", joinColumns = @JoinColumn(name = "animal_id"))
	@Enumerated(EnumType.STRING)
	@Column(name = "tag", nullable = false)
	@OrderColumn(name = "ordem")
	private List<Tag> tags = new ArrayList<>();

	@ElementCollection
	@CollectionTable(name = "animal_logs", joinColumns = @JoinColumn(name = "animal_id"))
	@Column(name = "mensagem", nullable = false, length = 1000)
	@OrderColumn(name = "ordem")
	private List<String> logs = new ArrayList<>();

	@Column(name = "cloudinary_public_id")
	private String cloudinaryPublicId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private AnimalStatus status;

	@ManyToMany
	@JoinTable(
			name = "animal_adotantes",
			joinColumns = @JoinColumn(name = "animal_id"),
			inverseJoinColumns = @JoinColumn(name = "adotante_id")
	)
	private Set<Adotante> adotantes = new LinkedHashSet<>();

	@ManyToOne
	@JoinColumn(name = "adotante_atual_id")
	private Adotante adotanteAtual;

	@Column(name = "lar_id", nullable = false)
	private UUID larId;

	@Column(name = "criado_em", nullable = false, updatable = false)
	private LocalDateTime criadoEm;

	@Column(name = "atualizado_em", nullable = false)
	private LocalDateTime atualizadoEm;

	protected Animal() {
	}

	public Animal(String nome, Especie especie, Porte porte, Integer idade, String descricao, UUID larId) {
		this(nome, especie, porte, idade, descricao, larId, List.of(), null);
	}

	public Animal(String nome, Especie especie, Porte porte, SexoAnimal sexo, Integer idade, String descricao, UUID larId) {
		this(nome, especie, porte, sexo, idade, descricao, larId, List.of(), null);
	}

	public Animal(
			String nome,
			Especie especie,
			Porte porte,
			Integer idade,
			String descricao,
			UUID larId,
			List<Tag> tags,
			String cloudinaryPublicId
	) {
		this(nome, especie, porte, null, idade, descricao, larId, tags, cloudinaryPublicId);
	}

	public Animal(
			String nome,
			Especie especie,
			Porte porte,
			SexoAnimal sexo,
			Integer idade,
			String descricao,
			UUID larId,
			List<Tag> tags,
			String cloudinaryPublicId
	) {
		validarDados(nome, especie, porte, idade, larId);
		this.nome = nome;
		this.especie = especie;
		this.porte = porte;
		this.sexo = sexo;
		this.idade = idade;
		this.descricao = descricao;
		this.larId = larId;
		this.tags = normalizarTags(tags);
		this.cloudinaryPublicId = normalizarTextoOpcional(cloudinaryPublicId);
		this.status = AnimalStatus.DISPONIVEL;
		this.criadoEm = LocalDateTime.now();
		this.atualizadoEm = this.criadoEm;
		this.logs.add("Animal cadastrado.");
	}

	public UUID getId() {
		return id;
	}

	public String getNome() {
		return nome;
	}

	public void transferirParaLar(UUID larId) {
		if (this.status != AnimalStatus.DISPONIVEL) {
			throw new IllegalStateException("Animal só pode ser transferido quando estiver disponível.");
		}
		validarLar(larId);
		this.larId = larId;
		adicionarLog("Animal transferido para outro lar.");
		this.atualizadoEm = LocalDateTime.now();
	}

	public void disponibilizar() {
		this.status = AnimalStatus.DISPONIVEL;
		adicionarLog("Animal disponibilizado para adoção.");
		this.atualizadoEm = LocalDateTime.now();
	}

	public void iniciarProcessoAdocao() {
		if (this.status != AnimalStatus.DISPONIVEL) {
			throw new IllegalStateException("Apenas animais disponíveis podem iniciar processo de adoção.");
		}
		this.status = AnimalStatus.EM_PROCESSO;
		adicionarLog("Processo de adoção iniciado.");
		this.atualizadoEm = LocalDateTime.now();
	}

	public void marcarComoAdotado() {
		if (this.status != AnimalStatus.EM_PROCESSO) {
			throw new IllegalStateException("Apenas animais em processo de adoção podem ser marcados como adotados.");
		}
		this.status = AnimalStatus.ADOTADO;
		adicionarLog("Animal marcado como adotado.");
		this.atualizadoEm = LocalDateTime.now();
	}

	public void realizarAdocao(Adotante adotante) {
		if (this.status != AnimalStatus.DISPONIVEL) {
			throw new IllegalStateException("Apenas animais disponíveis podem ser adotados.");
		}
		if (adotante == null) {
			throw new IllegalArgumentException("Adotante é obrigatório.");
		}

		this.adotanteAtual = adotante;
		this.adotantes.add(adotante);
		this.status = AnimalStatus.ADOTADO;
		adicionarLog("Adotado por " + adotante.getNome() + ".");
	}

	public Adotante devolver(String motivo) {
		if (this.status != AnimalStatus.ADOTADO || this.adotanteAtual == null) {
			throw new IllegalStateException("Apenas animais adotados podem ser devolvidos.");
		}
		Adotante adotanteAnterior = this.adotanteAtual;
		this.adotanteAtual = null;
		this.status = AnimalStatus.DISPONIVEL;
		String motivoNormalizado = normalizarTextoOpcional(motivo);
		if (motivoNormalizado == null) {
			adicionarLog("Animal devolvido por " + adotanteAnterior.getNome() + ".");
		} else {
			adicionarLog("Animal devolvido por " + adotanteAnterior.getNome() + ". Motivo: " + motivoNormalizado + ".");
		}
		this.atualizadoEm = LocalDateTime.now();
		return adotanteAnterior;
	}

	public void adicionarLog(String mensagem) {
		String mensagemNormalizada = normalizarTextoOpcional(mensagem);
		if (mensagemNormalizada == null) {
			throw new IllegalArgumentException("Mensagem do log é obrigatória.");
		}
		this.logs.add(mensagemNormalizada);
		this.atualizadoEm = LocalDateTime.now();
	}

	public Especie getEspecie() {
		return especie;
	}

	public Porte getPorte() {
		return porte;
	}

	public SexoAnimal getSexo() {
		return sexo;
	}

	public Integer getIdade() {
		return idade;
	}

	public String getDescricao() {
		return descricao;
	}

	public List<Tag> getTags() {
		return List.copyOf(tags);
	}

	public List<String> getLogs() {
		return List.copyOf(logs);
	}

	public String getCloudinaryPublicId() {
		return cloudinaryPublicId;
	}

	public AnimalStatus getStatus() {
		return status;
	}

	public Set<Adotante> getAdotantes() {
		return Set.copyOf(adotantes);
	}

	public Adotante getAdotanteAtual() {
		return adotanteAtual;
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

	private List<Tag> normalizarTags(List<Tag> tags) {
		if (tags == null) {
			return new ArrayList<>();
		}
		List<Tag> tagsNormalizadas = tags.stream()
				.filter(Objects::nonNull)
				.distinct()
				.toList();
		return new ArrayList<>(tagsNormalizadas);
	}

	private String normalizarTextoOpcional(String texto) {
		if (texto == null || texto.isBlank()) {
			return null;
		}
		return texto.trim();
	}
}
