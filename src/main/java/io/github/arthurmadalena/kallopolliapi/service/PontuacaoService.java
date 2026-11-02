package io.github.arthurmadalena.kallopolliapi.service;

import io.github.arthurmadalena.kallopolliapi.DTOs.ColaboradorPontosResponse;
import io.github.arthurmadalena.kallopolliapi.DTOs.RankingItemResponse;
import io.github.arthurmadalena.kallopolliapi.entities.ColaboradorEntity;
import io.github.arthurmadalena.kallopolliapi.entities.Periodo;
import io.github.arthurmadalena.kallopolliapi.entities.PontuacaoEventoEntity;
import io.github.arthurmadalena.kallopolliapi.entities.TipoPontuacao;
import io.github.arthurmadalena.kallopolliapi.repositories.ColaboradorRepository;
import io.github.arthurmadalena.kallopolliapi.repositories.PontuacaoEventoRepository;
import io.github.arthurmadalena.kallopolliapi.shared.RecursoNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class PontuacaoService {

    public static final int PONTOS_RESPOSTA_IA = 2;
    private static final int MIN_CARACTERES = 5;

    private final PontuacaoEventoRepository repository;
    private final ColaboradorRepository colaboradorRepository;

    public PontuacaoService(PontuacaoEventoRepository repository,
                            ColaboradorRepository colaboradorRepository) {
        this.repository = repository;
        this.colaboradorRepository = colaboradorRepository;
    }

    @Transactional
    public void lancar(ColaboradorEntity colaborador, TipoPontuacao tipo,
                       int pontos, String referencia) {
        var e = new PontuacaoEventoEntity();
        e.setColaborador(colaborador);
        e.setTipo(tipo);
        e.setPontos(pontos);
        e.setReferencia(referencia);
        repository.save(e);
    }

    // Vale no máximo 1 vez por dia, e só para mensagens com um tamanho mínimo
    @Transactional
    public void registrarRespostaIA(ColaboradorEntity colaborador, String texto) {
        if (texto == null || texto.trim().length() < MIN_CARACTERES) return;

        var inicioDoDia = LocalDate.now().atStartOfDay();
        boolean jaGanhouHoje = repository
                .existsByColaboradorIdAndTipoAndCriadoEmGreaterThanEqual(
                        colaborador.getId(), TipoPontuacao.RESPOSTA_IA, inicioDoDia);
        if (jaGanhouHoje) return;

        lancar(colaborador, TipoPontuacao.RESPOSTA_IA, PONTOS_RESPOSTA_IA, "mensagem");
    }

    @Transactional(readOnly = true)
    public int totalDe(Long colaboradorId) {
        return repository.totalDe(colaboradorId).intValue();
    }

    @Transactional(readOnly = true)
    public List<RankingItemResponse> top(Periodo periodo, Long lojaId) {
        return montarRanking(periodo, lojaId).stream().limit(10).toList();
    }

    @Transactional(readOnly = true)
    public ColaboradorPontosResponse resumo(Long colaboradorId) {
        var c = colaboradorRepository.findById(colaboradorId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Colaborador não encontrado"));

        int posicao = 0;
        for (var item : montarRanking(Periodo.TOTAL, null)) {
            if (item.colaboradorId().equals(colaboradorId)) {
                posicao = item.posicao();
            }
        }
        return new ColaboradorPontosResponse(c.getId(), c.getColaboradorName(),
                totalDe(colaboradorId), posicao);
    }

    private List<RankingItemResponse> montarRanking(Periodo periodo, Long lojaId) {
        var desde = inicioDo(periodo);
        var linhas = (lojaId == null)
                ? repository.rankingGeral(desde)
                : repository.rankingPorLoja(desde, lojaId);

        var lista = new ArrayList<RankingItemResponse>();
        int posicao = 1;
        for (var l : linhas) {
            lista.add(new RankingItemResponse(posicao++, l.getColaboradorId(),
                    l.getNome(), l.getPontos()));
        }
        return lista;
    }

    private LocalDateTime inicioDo(Periodo periodo) {
        return switch (periodo) {
            case SEMANA -> LocalDate.now().with(DayOfWeek.MONDAY).atStartOfDay();
            case MES -> LocalDate.now().withDayOfMonth(1).atStartOfDay();
            case TOTAL -> LocalDateTime.of(2000, 1, 1, 0, 0);
        };
    }
}