package io.github.arthurmadalena.kallopolliapi.service;

import io.github.arthurmadalena.kallopolliapi.DTOs.ColaboradorPontosResponse;
import io.github.arthurmadalena.kallopolliapi.DTOs.CursoRequest;
import io.github.arthurmadalena.kallopolliapi.DTOs.CursoResponse;
import io.github.arthurmadalena.kallopolliapi.entities.ConclusaoCursoEntity;
import io.github.arthurmadalena.kallopolliapi.entities.CursoEntity;
import io.github.arthurmadalena.kallopolliapi.entities.TipoPontuacao;
import io.github.arthurmadalena.kallopolliapi.repositories.ColaboradorRepository;
import io.github.arthurmadalena.kallopolliapi.repositories.ConclusaoCursoRepository;
import io.github.arthurmadalena.kallopolliapi.repositories.CursoRepository;
import io.github.arthurmadalena.kallopolliapi.shared.RecursoNaoEncontradoException;
import io.github.arthurmadalena.kallopolliapi.shared.RegraNegocioException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CursoService {

    private final CursoRepository cursoRepository;
    private final ConclusaoCursoRepository conclusaoRepository;
    private final ColaboradorRepository colaboradorRepository;
    private final PontuacaoService pontuacaoService;

    public CursoService(CursoRepository cursoRepository,
                        ConclusaoCursoRepository conclusaoRepository,
                        ColaboradorRepository colaboradorRepository,
                        PontuacaoService pontuacaoService) {
        this.cursoRepository = cursoRepository;
        this.conclusaoRepository = conclusaoRepository;
        this.colaboradorRepository = colaboradorRepository;
        this.pontuacaoService = pontuacaoService;
    }

    @Transactional
    public CursoResponse criar(CursoRequest req) {
        var c = new CursoEntity();
        c.setTitulo(req.titulo());
        c.setPontos(req.pontos());
        return toResponse(cursoRepository.save(c));
    }

    @Transactional(readOnly = true)
    public List<CursoResponse> listar() {
        return cursoRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public ColaboradorPontosResponse concluir(Long cursoId, Long colaboradorId) {
        var curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Curso não encontrado"));
        var colaborador = colaboradorRepository.findById(colaboradorId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Colaborador não encontrado"));

        if (conclusaoRepository.existsByColaboradorIdAndCursoId(colaboradorId, cursoId)) {
            throw new RegraNegocioException("Este curso já foi concluído por este colaborador");
        }

        var conclusao = new ConclusaoCursoEntity();
        conclusao.setColaborador(colaborador);
        conclusao.setCurso(curso);
        conclusaoRepository.save(conclusao);

        pontuacaoService.lancar(colaborador, TipoPontuacao.CURSO_CONCLUIDO,
                curso.getPontos(), "curso:" + curso.getId());

        return pontuacaoService.resumo(colaboradorId);
    }

    private CursoResponse toResponse(CursoEntity c) {
        return new CursoResponse(c.getId(), c.getTitulo(), c.getPontos());
    }
}