package com.Miaumigo.Miaumigo.repository;

import com.Miaumigo.Miaumigo.domain.SolicitacaoAdocao;
import com.Miaumigo.Miaumigo.domain.SolicitacaoStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SolicitacaoAdocaoRepository extends JpaRepository<SolicitacaoAdocao, UUID> {

	boolean existsByAnimalIdAndAdotanteIdAndStatus(UUID animalId, UUID adotanteId, SolicitacaoStatus status);

	List<SolicitacaoAdocao> findByAdotanteIdOrderByCriadoEmDesc(UUID adotanteId);

	List<SolicitacaoAdocao> findByAnimalIdAndStatus(UUID animalId, SolicitacaoStatus status);

	List<SolicitacaoAdocao> findByAnimalLarIdAndStatus(UUID larId, SolicitacaoStatus status);
}
