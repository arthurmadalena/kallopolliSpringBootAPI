package io.github.arthurmadalena.kallopolliapi.entities;


import jakarta.persistence.*;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

@Entity
@Table(name = "mensagem")
public class MensagemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "colaborador_id")
    private ColaboradorEntity colaborador;

    @Enumerated(EnumType.STRING)
    private DirecaoMensagem direcao;

    private String texto;
    private String intencao;
    private String categoria;
    private String alvo;

    private LocalDateTime criadaEm = LocalDateTime.now();

    public MensagemEntity() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ColaboradorEntity getColaborador() { return colaborador; }
    public void setColaborador(ColaboradorEntity colaborador) { this.colaborador = colaborador; }

    public DirecaoMensagem getDirecao() { return direcao; }
    public void setDirecao(DirecaoMensagem direcao) { this.direcao = direcao; }

    public String getTexto() { return texto; }
    public void setTexto(String texto) { this.texto = texto; }

    public String getIntencao() { return intencao; }
    public void setIntencao(String intencao) { this.intencao = intencao; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getAlvo() { return alvo; }
    public void setAlvo(String alvo) { this.alvo = alvo; }

    public LocalDateTime getCriadaEm() { return criadaEm; }
    public void setCriadaEm(LocalDateTime criadaEm) { this.criadaEm = criadaEm; }
}