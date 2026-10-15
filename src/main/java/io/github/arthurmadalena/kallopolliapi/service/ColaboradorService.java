package io.github.arthurmadalena.kallopolliapi.service;

import io.github.arthurmadalena.kallopolliapi.DTOs.ColaboradorRequest;
import io.github.arthurmadalena.kallopolliapi.DTOs.ColaboradorResponse;
import io.github.arthurmadalena.kallopolliapi.entities.ColaboradorEntity;
import io.github.arthurmadalena.kallopolliapi.repositories.ColaboradorRepository;
import io.github.arthurmadalena.kallopolliapi.repositories.LojaRepository;
import io.github.arthurmadalena.kallopolliapi.shared.RecursoNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ColaboradorService {
    private final ColaboradorRepository repository;
    private final LojaRepository lojaRepository;

    public ColaboradorService(ColaboradorRepository repository, LojaRepository lojaRepository) {
        this.repository = repository;
        this.lojaRepository = lojaRepository;
    }

    @Transactional
    public ColaboradorResponse criar(ColaboradorRequest req) {
        var loja = lojaRepository.findById(req.lojaId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Loja não encontrada"));

        var c = new ColaboradorEntity();
        c.setColaboradorName(req.colaboradorName());
        c.setColaboradorTelefone(req.colaboradorTelefone());
        c.setPapel(req.papel());
        c.setLoja(loja);
        c.setDataAdmissao(req.dataAdmissao());

        if (req.gestorId() != null) {
            var gestor = repository.findById(req.gestorId())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Gestor não encontrado"));
            c.setGestor(gestor);
        }

        return toResponse(repository.save(c));
    }

    @Transactional(readOnly = true)
    public List<ColaboradorResponse> listar() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    private ColaboradorResponse toResponse(ColaboradorEntity c) {
        return new ColaboradorResponse(c.getId(), c.getColaboradorName(), c.getColaboradorTelefone(),
                c.getPapel(), c.getLoja().getId());
    }
}
