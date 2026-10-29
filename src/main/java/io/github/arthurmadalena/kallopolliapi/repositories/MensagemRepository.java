package io.github.arthurmadalena.kallopolliapi.repositories;

import org.hibernate.engine.spi.ManagedEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MensagemRepository extends JpaRepository<ManagedEntity, Long> {
    List<ManagedEntity> findByColaboradorIdOrderByCriadaEmAsc(Long colaboradorId);
}
