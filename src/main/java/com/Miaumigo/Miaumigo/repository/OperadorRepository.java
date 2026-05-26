package com.Miaumigo.Miaumigo.repository;

import com.Miaumigo.Miaumigo.domain.Operador;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OperadorRepository extends JpaRepository<Operador, UUID> {
}
