package io.github.arthurmadalena.kallopolliapi.entities;



import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "relato")
public class RelatoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "colaborador_id", nullable = false)
    private ColaboradorEntity colaborador;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gestor_citado_id")
    private ColaboradorEntity gestorCitado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destinatario_id")
    private ColaboradorEntity destinatario;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descricao;

    private String categoria;

    @Column(nullable = false)
    private String status = "AGUARDANDO_CONFIRMACAO";

    @Column(name = "criado_em")
    private LocalDateTime criadoEm = LocalDateTime.now();

    @Column(name = "confirmado_em")
    private LocalDateTime confirmadoEm;

    public Long getId() { return id; }

    public ColaboradorEntity getColaborador() { return colaborador; }
    public void setColaborador(ColaboradorEntity colaborador) { this.colaborador = colaborador; }

    public ColaboradorEntity getGestorCitado() { return gestorCitado; }
    public void setGestorCitado(ColaboradorEntity gestorCitado) { this.gestorCitado = gestorCitado; }

    public ColaboradorEntity getDestinatario() { return destinatario; }
    public void setDestinatario(ColaboradorEntity destinatario) { this.destinatario = destinatario; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }

    public LocalDateTime getConfirmadoEm() { return confirmadoEm; }
    public void setConfirmadoEm(LocalDateTime confirmadoEm) { this.confirmadoEm = confirmadoEm; }
}
