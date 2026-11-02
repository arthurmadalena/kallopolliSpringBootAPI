package io.github.arthurmadalena.kallopolliapi.repositories;

import io.github.arthurmadalena.kallopolliapi.entities.MentoriaEntity;
import io.github.arthurmadalena.kallopolliapi.entities.StatusMentoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface MentoriaRepository extends JpaRepository<MentoriaEntity, Long> {
    long countByMentorIdAndStatusIn(Long mentorId, Collection<StatusMentoria> status);
    boolean existsByMentoradoIdAndStatusIn(Long mentoradoId, Collection<StatusMentoria> status);
}