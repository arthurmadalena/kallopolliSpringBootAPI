
package io.github.arthurmadalena.kallopolliapi.service;

import io.github.arthurmadalena.kallopolliapi.entities.ConteudoEntity;
import io.github.arthurmadalena.kallopolliapi.repositories.ConteudoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ConteudoService {

    private final ConteudoRepository repository;

    public ConteudoService(ConteudoRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<ConteudoEntity> buscarPorCategoria(String categoria) {

        if (categoria == null || categoria.isBlank()) {
            return repository.findByAtivoTrue();
        }

        return repository.findByCategoriaIgnoreCaseAndAtivoTrue(
                categoria.trim()
        );
    }

    @Transactional(readOnly = true)
    public List<ConteudoEntity> listarAtivos() {
        return repository.findByAtivoTrue();
    }
}

