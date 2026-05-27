package com.Miaumigo.Miaumigo.repository;

import com.Miaumigo.Miaumigo.domain.Lar;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LarRepository extends JpaRepository<Lar, UUID> {
}
