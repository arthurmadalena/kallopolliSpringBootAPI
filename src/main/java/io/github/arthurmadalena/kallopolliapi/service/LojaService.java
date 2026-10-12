package io.github.arthurmadalena.kallopolliapi.service;

import io.github.arthurmadalena.kallopolliapi.DTOs.LojaRequest;
import io.github.arthurmadalena.kallopolliapi.DTOs.LojaResponse;
import io.github.arthurmadalena.kallopolliapi.entities.LojaEntity;
import io.github.arthurmadalena.kallopolliapi.repositories.LojaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LojaService {
    private final LojaRepository repository;

    public LojaService(LojaRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public LojaResponse criar(LojaRequest req) {
        var loja = new LojaEntity();
        loja.setLojaName(req.lojaName());
        loja.setLojaCity(req.lojaCity());
        var salva = repository.save(loja);
        return new LojaResponse(salva.getId(), salva.getLojaName(), salva.getLojaCity());
    }

    @Transactional(readOnly = true)
    public List<LojaResponse> listar() {
        return repository.findAll().stream()
                .map(l -> new LojaResponse(l.getId(), l.getLojaName(), l.getLojaCity()))
                .toList();
    }
}
