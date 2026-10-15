package io.github.arthurmadalena.kallopolliapi.entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "jornada")
public class JornadaEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "colaborador_id")
    private ColaboradorEntity colaborador;

    private LocalDateTime dataInicio;

    @Enumerated(EnumType.STRING)
    private StatusJornada status = StatusJornada.ATIVA;

    private int tentativasSemRespostas = 0;

    private LocalDateTime ultimaMensagemEm;

    public JornadaEntity() {
    }

    public JornadaEntity(Long id, ColaboradorEntity colaborador, LocalDateTime dataInicio, StatusJornada status, int tentativasSemRespostas, LocalDateTime ultimaMensagemEm) {
        this.id = id;
        this.colaborador = colaborador;
        this.dataInicio = dataInicio;
        this.status = status;
        this.tentativasSemRespostas = tentativasSemRespostas;
        this.ultimaMensagemEm = ultimaMensagemEm;
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

    public LocalDateTime getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDateTime dataInicio) {
        this.dataInicio = dataInicio;
    }

    public StatusJornada getStatus() {
        return status;
    }

    public void setStatus(StatusJornada status) {
        this.status = status;
    }

    public int getTentativasSemRespostas() {
        return tentativasSemRespostas;
    }

    public void setTentativasSemRespostas(int tentativasSemRespostas) {
        this.tentativasSemRespostas = tentativasSemRespostas;
    }

    public LocalDateTime getUltimaMensagemEm() {
        return ultimaMensagemEm;
    }

    public void setUltimaMensagemEm(LocalDateTime ultimaMensagemEm) {
        this.ultimaMensagemEm = ultimaMensagemEm;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        JornadaEntity that = (JornadaEntity) o;
        return tentativasSemRespostas == that.tentativasSemRespostas && Objects.equals(id, that.id) && Objects.equals(colaborador, that.colaborador) && Objects.equals(dataInicio, that.dataInicio) && status == that.status && Objects.equals(ultimaMensagemEm, that.ultimaMensagemEm);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, colaborador, dataInicio, status, tentativasSemRespostas, ultimaMensagemEm);
    }

    @Override
    public String toString() {
        return "JornadaEntity{" +
                "id=" + id +
                ", colaborador=" + colaborador +
                ", dataInicio=" + dataInicio +
                ", status=" + status +
                ", tentativasSemRespostas=" + tentativasSemRespostas +
                ", ultimaMensagemEm=" + ultimaMensagemEm +
                '}';
    }
}
