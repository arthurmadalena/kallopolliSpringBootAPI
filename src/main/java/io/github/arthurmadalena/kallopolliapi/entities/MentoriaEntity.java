package io.github.arthurmadalena.kallopolliapi.entities;

import jakarta.persistence.*;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

@Entity
@Table(name = "mentoria")
public class MentoriaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_id")
    private ColaboradorEntity mentor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentorado_id")
    private ColaboradorEntity mentorado;

    @Enumerated(EnumType.STRING)
    private StatusMentoria status = StatusMentoria.SUGERIDA;

    private LocalDateTime criadaEm = LocalDateTime.now();
    private LocalDateTime decididaEm;

    public MentoriaEntity() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ColaboradorEntity getMentor() { return mentor; }
    public void setMentor(ColaboradorEntity mentor) { this.mentor = mentor; }

    public ColaboradorEntity getMentorado() { return mentorado; }
    public void setMentorado(ColaboradorEntity mentorado) { this.mentorado = mentorado; }

    public StatusMentoria getStatus() { return status; }
    public void setStatus(StatusMentoria status) { this.status = status; }

    public LocalDateTime getCriadaEm() { return criadaEm; }
    public void setCriadaEm(LocalDateTime criadaEm) { this.criadaEm = criadaEm; }

    public LocalDateTime getDecididaEm() { return decididaEm; }
    public void setDecididaEm(LocalDateTime decididaEm) { this.decididaEm = decididaEm; }
}