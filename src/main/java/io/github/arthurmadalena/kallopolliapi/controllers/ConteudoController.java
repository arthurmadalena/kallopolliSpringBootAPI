
package io.github.arthurmadalena.kallopolliapi.controllers;

import io.github.arthurmadalena.kallopolliapi.DTOs.ConteudoRequest;
import io.github.arthurmadalena.kallopolliapi.DTOs.ConteudoResponse;
import io.github.arthurmadalena.kallopolliapi.entities.ConteudoEntity;
import io.github.arthurmadalena.kallopolliapi.service.ConteudoService;
import io.github.arthurmadalena.kallopolliapi.repositories.ConteudoRepository;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/conteudos")
public class ConteudoController {

    private final ConteudoService service;
    private final ConteudoRepository repository;

    public ConteudoController(
            ConteudoService service,
            ConteudoRepository repository
    ) {
        this.service = service;
        this.repository = repository;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ConteudoResponse criar(@Valid @RequestBody ConteudoRequest req) {

        ConteudoEntity conteudo = new ConteudoEntity();

        conteudo.setTitulo(req.titulo().trim());
        conteudo.setCategoria(req.categoria().trim());
        conteudo.setTexto(req.texto().trim());
        conteudo.setAtivo(true);

        return converter(repository.save(conteudo));
    }

    @GetMapping
    public List<ConteudoResponse> listar() {

        return service.listarAtivos()
                .stream()
                .map(this::converter)
                .toList();
    }

    @GetMapping("/categoria/{categoria}")
    public List<ConteudoResponse> buscarPorCategoria(
            @PathVariable String categoria
    ) {

        return service.buscarPorCategoria(categoria)
                .stream()
                .map(this::converter)
                .toList();
    }

    @PatchMapping("/{id}/desativar")
    public ConteudoResponse desativar(@PathVariable Long id) {

        ConteudoEntity conteudo = repository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Conteúdo não encontrado."));

        conteudo.setAtivo(false);

        return converter(repository.save(conteudo));
    }

    private ConteudoResponse converter(ConteudoEntity c) {

        return new ConteudoResponse(
                c.getId(),
                c.getTitulo(),
                c.getCategoria(),
                c.getTexto(),
                c.isAtivo()
        );
    }
}
