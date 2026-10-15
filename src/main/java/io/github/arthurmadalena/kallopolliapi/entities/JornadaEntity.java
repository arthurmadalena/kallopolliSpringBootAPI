package io.github.arthurmadalena.kallopolliapi.entities;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "jornada")
public class JornadaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "colaborador_id")
    private ColaboradorEntity colaborador;

    private LocalDate dataInicio;

    @Enumerated(EnumType.STRING)
    private StatusJornada status = StatusJornada.ATIVA;

    private int tentativasSemResposta = 0;

    private LocalDateTime ultimaMensagemEm;

    public JornadaEntity() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ColaboradorEntity getColaborador() {
        return colaborador;
    }

    public void setColaborador(ColaboradorEntity colaborador) {
        this.colaborador = colaborador;
    }

    public LocalDate getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDate dataInicio) {
        this.dataInicio = dataInicio;
    }

    public StatusJornada getStatus() {
        return status;
    }

    public void setStatus(StatusJornada status) {
        this.status = status;
    }

    public int getTentativasSemResposta() {
        return tentativasSemResposta;
    }

    public void setTentativasSemResposta(int tentativasSemResposta) {
        this.tentativasSemResposta = tentativasSemResposta;
    }

    public LocalDateTime getUltimaMensagemEm() {
        return ultimaMensagemEm;
    }

    public void setUltimaMensagemEm(LocalDateTime ultimaMensagemEm) {
        this.ultimaMensagemEm = ultimaMensagemEm;
    }
}