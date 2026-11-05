package io.github.arthurmadalena.kallopolliapi.repositories;

import io.github.arthurmadalena.kallopolliapi.entities.ConteudoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConteudoRepository extends JpaRepository<ConteudoEntity, Long> {

    List<ConteudoEntity> findByCategoriaIgnoreCaseAndAtivoTrue(String categoria);

    List<ConteudoEntity> findByAtivoTrue();
}