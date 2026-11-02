package io.github.arthurmadalena.kallopolliapi.entities;

import jakarta.persistence.*;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

@Entity
@Table(name = "pontuacao_evento")
public class PontuacaoEventoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "colaborador_id")
    private ColaboradorEntity colaborador;

    @Enumerated(EnumType.STRING)
    private TipoPontuacao tipo;

    private int pontos;
    private String referencia;
    private LocalDateTime criadoEm = LocalDateTime.now();

    public PontuacaoEventoEntity() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ColaboradorEntity getColaborador() { return colaborador; }
    public void setColaborador(ColaboradorEntity colaborador) { this.colaborador = colaborador; }

    public TipoPontuacao getTipo() { return tipo; }
    public void setTipo(TipoPontuacao tipo) { this.tipo = tipo; }

    public int getPontos() { return pontos; }
    public void setPontos(int pontos) { this.pontos = pontos; }

    public String getReferencia() { return referencia; }
    public void setReferencia(String referencia) { this.referencia = referencia; }

    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }
}