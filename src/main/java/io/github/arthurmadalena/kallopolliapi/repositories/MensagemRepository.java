package io.github.arthurmadalena.kallopolliapi.repositories;

import io.github.arthurmadalena.kallopolliapi.entities.MensagemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MensagemRepository extends JpaRepository<MensagemEntity, Long> {
    List<MensagemEntity> findByColaboradorIdOrderByCriadaEmAsc(Long colaboradorId);
}
