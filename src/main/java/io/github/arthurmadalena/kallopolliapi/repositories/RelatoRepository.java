package io.github.arthurmadalena.kallopolliapi.repositories;

import io.github.arthurmadalena.kallopolliapi.entities.RelatoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RelatoRepository extends JpaRepository<RelatoEntity, Long> {

    Optional<RelatoEntity>
    findFirstByColaboradorIdAndStatusOrderByCriadoEmDesc(
            Long colaboradorId,
            String status
    );

    List<RelatoEntity> findAllByOrderByCriadoEmDesc();

    long countByStatus(String status);
}
