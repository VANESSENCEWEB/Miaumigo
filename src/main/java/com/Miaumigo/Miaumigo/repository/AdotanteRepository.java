package com.Miaumigo.Miaumigo.repository;

import com.Miaumigo.Miaumigo.domain.Adotante;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AdotanteRepository extends JpaRepository<Adotante, UUID> {
}
