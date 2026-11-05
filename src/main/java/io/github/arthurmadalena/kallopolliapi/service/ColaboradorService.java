

package io.github.arthurmadalena.kallopolliapi.service;

import io.github.arthurmadalena.kallopolliapi.DTOs.ColaboradorRequest;
import io.github.arthurmadalena.kallopolliapi.DTOs.ColaboradorResponse;
import io.github.arthurmadalena.kallopolliapi.entities.ColaboradorEntity;
import io.github.arthurmadalena.kallopolliapi.entities.Papel;
import io.github.arthurmadalena.kallopolliapi.repositories.ColaboradorRepository;
import io.github.arthurmadalena.kallopolliapi.repositories.LojaRepository;
import io.github.arthurmadalena.kallopolliapi.shared.RecursoNaoEncontradoException;
import io.github.arthurmadalena.kallopolliapi.shared.RegraNegocioException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ColaboradorService {

    private final ColaboradorRepository repository;
    private final LojaRepository lojaRepository;
    private final JornadaService jornadaService;

    public ColaboradorService(
            ColaboradorRepository repository,
            LojaRepository lojaRepository,
            JornadaService jornadaService
    ) {
        this.repository = repository;
        this.lojaRepository = lojaRepository;
        this.jornadaService = jornadaService;
    }

    @Transactional
    public ColaboradorResponse criar(ColaboradorRequest req) {

        String telefone = normalizarTelefone(req.colaboradorTelefone());

        if (repository.existsByColaboradorTelefone(telefone)) {
            throw new RegraNegocioException(
                    "Já existe um colaborador cadastrado com esse telefone."
            );
        }

        var loja = lojaRepository.findById(req.lojaId())
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException("Loja não encontrada."));

        ColaboradorEntity colaborador = new ColaboradorEntity();

        colaborador.setColaboradorName(req.colaboradorName().trim());
        colaborador.setColaboradorTelefone(telefone);
        colaborador.setPapel(req.papel());
        colaborador.setLoja(loja);
        colaborador.setDataAdmissao(req.dataAdmissao());
        colaborador.setFuncao(req.funcao());
        colaborador.setMentorDisponivel(
                Boolean.TRUE.equals(req.mentorDisponivel())
        );

        if (req.gestorId() != null) {

            ColaboradorEntity gestor = repository.findById(req.gestorId())
                    .orElseThrow(() ->
                            new RecursoNaoEncontradoException("Gestor não encontrado."));

            if (!gestor.isAtivo()) {
                throw new RegraNegocioException(
                        "Não é possível vincular um gestor inativo."
                );
            }

            colaborador.setGestor(gestor);
        }

        if (req.papel() == Papel.COLABORADOR && req.gestorId() == null) {
            throw new RegraNegocioException(
                    "Um colaborador precisa ter um gestor cadastrado."
            );
        }

        ColaboradorEntity salvo = repository.save(colaborador);

        if (salvo.getPapel() == Papel.COLABORADOR) {
            jornadaService.criarPara(salvo);
        }

        return toResponse(salvo);
    }

    @Transactional(readOnly = true)
    public List<ColaboradorResponse> listar() {

        return repository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private String normalizarTelefone(String telefone) {

        if (telefone == null) {
            throw new RegraNegocioException("Telefone é obrigatório.");
        }

        String normalizado = telefone.replaceAll("\\D", "");

        if (normalizado.length() < 10 || normalizado.length() > 13) {
            throw new RegraNegocioException(
                    "Telefone inválido. Informe DDD e número."
            );
        }

        return normalizado;
    }

    private ColaboradorResponse toResponse(ColaboradorEntity c) {

        return new ColaboradorResponse(
                c.getId(),
                c.getColaboradorName(),
                c.getColaboradorTelefone(),
                c.getPapel(),
                c.getLoja().getId(),
                c.getFuncao(),
                c.isMentorDisponivel()
        );
    }
}
