package com.Miaumigo.Miaumigo.dto;

import com.Miaumigo.Miaumigo.domain.Especie;
import com.Miaumigo.Miaumigo.domain.ExperienciaAnimais;
import com.Miaumigo.Miaumigo.domain.Porte;
import com.Miaumigo.Miaumigo.domain.Tag;
import com.Miaumigo.Miaumigo.domain.TempoDisponivel;
import com.Miaumigo.Miaumigo.domain.TipoMoradia;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record PerfilAdotanteRequest(
		@JsonProperty("especies_preferidas")
		List<Especie> especiesPreferidas,

		List<Tag> preferencias,

		@JsonProperty("tipo_moradia")
		TipoMoradia tipoMoradia,

		@JsonProperty("espaco_disponivel")
		Porte espacoDisponivel,

		@JsonProperty("tempo_disponivel")
		TempoDisponivel tempoDisponivel,

		@JsonProperty("experiencia_animais")
		ExperienciaAnimais experienciaAnimais,

		@JsonProperty("possui_criancas")
		Boolean possuiCriancas,

		@JsonProperty("possui_caes")
		Boolean possuiCaes,

		@JsonProperty("possui_gatos")
		Boolean possuiGatos,

		String telefone,

		String cidade
) {
}
