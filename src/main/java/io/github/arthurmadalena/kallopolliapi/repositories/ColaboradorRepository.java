package io.github.arthurmadalena.kallopolliapi.repositories;

import io.github.arthurmadalena.kallopolliapi.entities.ColaboradorEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ColaboradorRepository extends JpaRepository<ColaboradorEntity, Long> {
    Optional<ColaboradorEntity> findByTelefone(String colaboradorTelefone);
}
