package com.Miaumigo.Miaumigo.repository;

import com.Miaumigo.Miaumigo.domain.MensagemSuporte;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MensagemSuporteRepository extends JpaRepository<MensagemSuporte, UUID> {
}
