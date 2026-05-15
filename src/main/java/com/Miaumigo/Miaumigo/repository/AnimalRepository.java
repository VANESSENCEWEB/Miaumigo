package com.Miaumigo.Miaumigo.repository;

import com.Miaumigo.Miaumigo.domain.Animal;
import com.Miaumigo.Miaumigo.domain.AnimalStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AnimalRepository extends JpaRepository<Animal, UUID> {

	List<Animal> findByLarId(UUID larId);

	List<Animal> findByStatus(AnimalStatus status);

	List<Animal> findByLarIdAndStatus(UUID larId, AnimalStatus status);
}
