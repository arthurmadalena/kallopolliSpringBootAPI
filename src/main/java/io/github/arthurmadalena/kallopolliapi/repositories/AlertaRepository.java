package io.github.arthurmadalena.kallopolliapi.repositories;

import io.github.arthurmadalena.kallopolliapi.entities.AlertaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AlertaRepository extends JpaRepository<AlertaEntity, Long> {

    List<AlertaEntity>
    findByDestinatarioColaboradorTelefoneAndStatusOrderByCriadoEmDesc(
            String telefone,
            String status
    );

    List<AlertaEntity> findByStatusOrderByCriadoEmDesc(String status);

    List<AlertaEntity>
    findByStatusAndPrazoLembreteEmBeforeAndLembreteEnviadoFalse(
            String status,
            LocalDateTime agora
    );

    long countByStatus(String status);
}