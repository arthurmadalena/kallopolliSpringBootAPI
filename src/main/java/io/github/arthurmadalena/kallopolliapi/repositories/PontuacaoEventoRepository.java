package io.github.arthurmadalena.kallopolliapi.repositories;

import io.github.arthurmadalena.kallopolliapi.entities.PontuacaoEventoEntity;
import io.github.arthurmadalena.kallopolliapi.entities.TipoPontuacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PontuacaoEventoRepository extends JpaRepository<PontuacaoEventoEntity, Long> {

    interface RankingLinha {
        Long getColaboradorId();
        String getNome();
        Long getPontos();
    }

    @Query("select coalesce(sum(e.pontos), 0L) from PontuacaoEventoEntity e "
            + "where e.colaborador.id = :id")
    Long totalDe(@Param("id") Long id);

    boolean existsByColaboradorIdAndTipoAndCriadoEmGreaterThanEqual(
            Long colaboradorId, TipoPontuacao tipo, LocalDateTime desde);

    @Query("""
        select e.colaborador.id as colaboradorId,
               e.colaborador.colaboradorName as nome,
               sum(e.pontos) as pontos
        from PontuacaoEventoEntity e
        where e.criadoEm >= :desde
        group by e.colaborador.id, e.colaborador.colaboradorName
        order by sum(e.pontos) desc
    """)
    List<RankingLinha> rankingGeral(@Param("desde") LocalDateTime desde);

    @Query("""
        select e.colaborador.id as colaboradorId,
               e.colaborador.colaboradorName as nome,
               sum(e.pontos) as pontos
        from PontuacaoEventoEntity e
        where e.criadoEm >= :desde
          and e.colaborador.loja.id = :lojaId
        group by e.colaborador.id, e.colaborador.colaboradorName
        order by sum(e.pontos) desc
    """)
    List<RankingLinha> rankingPorLoja(@Param("desde") LocalDateTime desde,
                                      @Param("lojaId") Long lojaId);
}
