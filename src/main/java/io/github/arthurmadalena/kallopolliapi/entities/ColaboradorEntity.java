package io.github.arthurmadalena.kallopolliapi.entities;

import jakarta.persistence.*;

import java.awt.print.Paper;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "colaborador")
public class ColaboradorEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String colaboradorName;
    private String colaboradorTelefone;

    @Enumerated(EnumType.STRING)
    private Paper papel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loja_id")
    private LojaEntity loja;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gestor_id")
    private ColaboradorEntity gestor;

    private LocalDate dataAdmissao;
    private boolean ativo = true;

    public ColaboradorEntity(Long id, String colaboradorName, String colaboradorTelefone, Paper papel, LojaEntity loja, ColaboradorEntity gestor, LocalDate dataAdmissao, boolean ativo) {
        this.id = id;
        this.colaboradorName = colaboradorName;
        this.colaboradorTelefone = colaboradorTelefone;
        this.papel = papel;
        this.loja = loja;
        this.gestor = gestor;
        this.dataAdmissao = dataAdmissao;
        this.ativo = ativo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getColaboradorName() {
        return colaboradorName;
    }

    public void setColaboradorName(String colaboradorName) {
        this.colaboradorName = colaboradorName;
    }

    public String getColaboradorTelefone() {
        return colaboradorTelefone;
    }

    public void setColaboradorTelefone(String colaboradorTelefone) {
        this.colaboradorTelefone = colaboradorTelefone;
    }

    public Paper getPapel() {
        return papel;
    }

    public void setPapel(Paper papel) {
        this.papel = papel;
    }

    public LojaEntity getLoja() {
        return loja;
    }

    public void setLoja(LojaEntity loja) {
        this.loja = loja;
    }

    public ColaboradorEntity getGestor() {
        return gestor;
    }

    public void setGestor(ColaboradorEntity gestor) {
        this.gestor = gestor;
    }

    public LocalDate getDataAdmissao() {
        return dataAdmissao;
    }

    public void setDataAdmissao(LocalDate dataAdmissao) {
        this.dataAdmissao = dataAdmissao;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ColaboradorEntity that = (ColaboradorEntity) o;
        return ativo == that.ativo && Objects.equals(id, that.id) && Objects.equals(colaboradorName, that.colaboradorName) && Objects.equals(colaboradorTelefone, that.colaboradorTelefone) && Objects.equals(papel, that.papel) && Objects.equals(loja, that.loja) && Objects.equals(gestor, that.gestor) && Objects.equals(dataAdmissao, that.dataAdmissao);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, colaboradorName, colaboradorTelefone, papel, loja, gestor, dataAdmissao, ativo);
    }

    @Override
    public String toString() {
        return "ColaboradorEntity{" +
                "id=" + id +
                ", colaboradorName='" + colaboradorName + '\'' +
                ", colaboradorTelefone='" + colaboradorTelefone + '\'' +
                ", papel=" + papel +
                ", loja=" + loja +
                ", gestor=" + gestor +
                ", dataAdmissao=" + dataAdmissao +
                ", ativo=" + ativo +
                '}';
    }
}
