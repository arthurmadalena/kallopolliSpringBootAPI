package io.github.arthurmadalena.kallopolliapi.service;

import io.github.arthurmadalena.kallopolliapi.DTOs.JornadaResponse;
import io.github.arthurmadalena.kallopolliapi.entities.ColaboradorEntity;
import io.github.arthurmadalena.kallopolliapi.entities.JornadaEntity;
import io.github.arthurmadalena.kallopolliapi.entities.StatusJornada;
import io.github.arthurmadalena.kallopolliapi.repositories.JornadaRepository;
import io.github.arthurmadalena.kallopolliapi.shared.RecursoNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class JornadaService {

    public static final int DURACAO_DIAS = 90;

    private final JornadaRepository repository;

    public JornadaService(JornadaRepository repository) {
        this.repository = repository;
    }

    // Chamado pelo ColaboradorService ao cadastrar um colaborador
    @Transactional
    public JornadaEntity criarPara(ColaboradorEntity colaborador) {
        var jornada = new JornadaEntity();
        jornada.setColaborador(colaborador);
        jornada.setDataInicio(colaborador.getDataAdmissao());
        jornada.setStatus(StatusJornada.ATIVA);
        return repository.save(jornada);
    }

    @Transactional(readOnly = true)
    public JornadaResponse buscarPorColaborador(Long colaboradorId) {
        var jornada = repository.findByColaboradorId(colaboradorId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Jornada não encontrada"));
        return toResponse(jornada);
    }

    @Transactional(readOnly = true)
    public List<JornadaResponse> listar() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    // Dia 1 = dia da admissão
    public int calcularDiaAtual(JornadaEntity jornada) {
        long dias = ChronoUnit.DAYS.between(jornada.getDataInicio(), LocalDate.now()) + 1;
        if (dias < 1) return 0;                  // ainda não começou
        return (int) Math.min(dias, DURACAO_DIAS);
    }

    private JornadaResponse toResponse(JornadaEntity j) {
        return new JornadaResponse(
                j.getId(),
                j.getColaborador().getId(),
                j.getColaborador().getColaboradorName(),
                j.getDataInicio(),
                calcularDiaAtual(j),
                j.getStatus(),
                j.getTentativasSemResposta()
        );
    }
}
