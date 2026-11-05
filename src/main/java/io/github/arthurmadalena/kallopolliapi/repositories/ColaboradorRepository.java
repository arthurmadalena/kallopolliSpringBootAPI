package io.github.arthurmadalena.kallopolliapi.repositories;

import io.github.arthurmadalena.kallopolliapi.entities.ColaboradorEntity;
import io.github.arthurmadalena.kallopolliapi.entities.Papel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ColaboradorRepository extends JpaRepository<ColaboradorEntity, Long> {
    Optional<ColaboradorEntity> findByColaboradorTelefone(String colaboradorTelefone);

    List<ColaboradorEntity> findByPapelAndLojaIdAndAtivoTrue(
            Papel papel,
            Long lojaId
    );

    boolean existsByColaboradorTelefone(String colaboradorTelefone);

    @Query("""
    select c from ColaboradorEntity c
    where lower(c.funcao) = lower(:funcao)
      and c.mentorDisponivel = true
      and c.ativo = true
      and c.id <> :excluirId
""")
    List<ColaboradorEntity> buscarCandidatosMentor(@Param("funcao") String funcao,
                                                   @Param("excluirId") Long excluirId);
}
