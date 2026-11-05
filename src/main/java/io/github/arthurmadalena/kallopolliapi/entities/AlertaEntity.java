package io.github.arthurmadalena.kallopolliapi.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "alerta")
public class AlertaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "relato_id", nullable = false)
    private RelatoEntity relato;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destinatario_id", nullable = false)
    private ColaboradorEntity destinatario;

    private String status = "PENDENTE";

    @Column(name = "criado_em")
    private LocalDateTime criadoEm = LocalDateTime.now();

    @Column(name = "prazo_lembrete_em")
    private LocalDateTime prazoLembreteEm;

    @Column(name = "lembrete_enviado")
    private boolean lembreteEnviado = false;

    @Column(name = "atendido_em")
    private LocalDateTime atendidoEm;

    @Column(columnDefinition = "TEXT")
    private String retorno;

    public Long getId() { return id; }

    public RelatoEntity getRelato() { return relato; }
    public void setRelato(RelatoEntity relato) { this.relato = relato; }

    public ColaboradorEntity getDestinatario() { return destinatario; }
    public void setDestinatario(ColaboradorEntity destinatario) { this.destinatario = destinatario; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }

    public LocalDateTime getPrazoLembreteEm() { return prazoLembreteEm; }
    public void setPrazoLembreteEm(LocalDateTime prazoLembreteEm) { this.prazoLembreteEm = prazoLembreteEm; }

    public boolean isLembreteEnviado() { return lembreteEnviado; }
    public void setLembreteEnviado(boolean lembreteEnviado) { this.lembreteEnviado = lembreteEnviado; }

    public LocalDateTime getAtendidoEm() { return atendidoEm; }
    public void setAtendidoEm(LocalDateTime atendidoEm) { this.atendidoEm = atendidoEm; }

    public String getRetorno() { return retorno; }
    public void setRetorno(String retorno) { this.retorno = retorno; }
}
