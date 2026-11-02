package io.github.arthurmadalena.kallopolliapi.repositories;

import io.github.arthurmadalena.kallopolliapi.entities.ConclusaoCursoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConclusaoCursoRepository extends JpaRepository<ConclusaoCursoEntity, Long> {
    boolean existsByColaboradorIdAndCursoId(Long colaboradorId, Long cursoId);
}