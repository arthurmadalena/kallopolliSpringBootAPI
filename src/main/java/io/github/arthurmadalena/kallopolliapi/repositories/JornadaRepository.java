package io.github.arthurmadalena.kallopolliapi.repositories;

import io.github.arthurmadalena.kallopolliapi.entities.JornadaEntity;
import io.github.arthurmadalena.kallopolliapi.entities.StatusJornada;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JornadaRepository extends JpaRepository<JornadaEntity, Long> {
    Optional<JornadaEntity> findByColaboradorId(Long colaboradorId);
    List<JornadaEntity> findByStatus(StatusJornada status);
}
