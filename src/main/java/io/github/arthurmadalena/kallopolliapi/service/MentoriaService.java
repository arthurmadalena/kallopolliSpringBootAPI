package io.github.arthurmadalena.kallopolliapi.service;

import io.github.arthurmadalena.kallopolliapi.entities.StatusMentoria;
import io.github.arthurmadalena.kallopolliapi.repositories.ColaboradorRepository;
import io.github.arthurmadalena.kallopolliapi.repositories.MentoriaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MentoriaService {

    private static final int MIN_MESES_DE_CASA = 6;
    private static final int MAX_MENTORADOS_ATIVOS = 2;
    private static final int PONTOS_MENTORIA_CONCLUIDA = 30;
    private static final List<StatusMentoria> ABERTAS =
            List.of(StatusMentoria.SUGERIDA, StatusMentoria.ATIVA);

    private final MentoriaRepository mentoriaRepository;
    private final ColaboradorRepository colaboradorRepository;
    private final PontuacaoService pontuacaoService;

    public MentoriaService(MentoriaRepository mentoriaRepository,
                           ColaboradorRepository colaboradorRepository,
                           PontuacaoService pontuacaoService) {
        this.mentoriaRepository = mentoriaRepository;
        this.colaboradorRepository = colaboradorRepository;
        this.pontuacaoService = pontuacaoService;
    }

    @Transactional(readOnly = true)
    public List<MentorSugestaoResponse> sugerir(Long mentoradoId) {
        var novato = buscar(mentoradoId);
        if (novato.getFuncao() == null || novato.getFuncao().isBlank()) {
            throw new RegraNegocioException("O colaborador não tem função cadastrada");
        }
        var hoje = LocalDate.now();

        return colaboradorRepository.buscarCandidatosMentor(novato.getFuncao(), novato.getId())
                .stream()
                .filter(c -> c.getPapel() == Papel.COLABORADOR)
                .filter(c -> !ehGestorDireto(novato, c))
                .map(c -> montarSugestao(novato, c, hoje))
                .filter(s -> s.mesesDeCasa() >= MIN_MESES_DE_CASA)
                .filter(s -> s.mentoradosAtivos() < MAX_MENTORADOS_ATIVOS)
                .sorted(Comparator.comparingInt(MentorSugestaoResponse::score).reversed())
                .limit(5)
                .toList();
    }

    @Transactional
    public MentoriaResponse criar(MentoriaRequest req) {
        if (req.mentorId().equals(req.mentoradoId())) {
            throw new RegraNegocioException("Mentor e mentorado não podem ser a mesma pessoa");
        }
        var mentor = buscar(req.mentorId());
        var mentorado = buscar(req.mentoradoId());

        if (!mentor.isMentorDisponivel()) {
            throw new RegraNegocioException("Este colaborador não está disponível como mentor");
        }
        if (mentoriaRepository.existsByMentoradoIdAndStatusIn(mentorado.getId(), ABERTAS)) {
            throw new RegraNegocioException("O colaborador já tem uma mentoria em andamento");
        }
        if (mentoriaRepository.countByMentorIdAndStatusIn(mentor.getId(), ABERTAS)
                >= MAX_MENTORADOS_ATIVOS) {
            throw new RegraNegocioException("O mentor já atingiu o limite de mentorados");
        }

        var m = new MentoriaEntity();
        m.setMentor(mentor);
        m.setMentorado(mentorado);
        return toResponse(mentoriaRepository.save(m));
    }

    @Transactional
    public MentoriaResponse atualizarStatus(Long id, StatusMentoria novo) {
        var m = mentoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Mentoria não encontrada"));

        validarTransicao(m.getStatus(), novo);
        m.setStatus(novo);
        m.setDecididaEm(LocalDateTime.now());

        if (novo == StatusMentoria.ENCERRADA) {
            pontuacaoService.lancar(m.getMentor(), TipoPontuacao.MENTORIA_CONCLUIDA,
                    PONTOS_MENTORIA_CONCLUIDA, "mentoria:" + m.getId());
        }
        return toResponse(mentoriaRepository.save(m));
    }

    @Transactional(readOnly = true)
    public List<MentoriaResponse> listar() {
        return mentoriaRepository.findAll().stream().map(this::toResponse).toList();
    }

    private void validarTransicao(StatusMentoria atual, StatusMentoria novo) {
        boolean ok = (atual == StatusMentoria.SUGERIDA
                && (novo == StatusMentoria.ATIVA || novo == StatusMentoria.RECUSADA))
                || (atual == StatusMentoria.ATIVA && novo == StatusMentoria.ENCERRADA);
        if (!ok) {
            throw new RegraNegocioException("Transição inválida: " + atual + " para " + novo);
        }
    }

    private boolean ehGestorDireto(ColaboradorEntity novato, ColaboradorEntity candidato) {
        return novato.getGestor() != null && novato.getGestor().getId().equals(candidato.getId());
    }

    private MentorSugestaoResponse montarSugestao(ColaboradorEntity novato,
                                                  ColaboradorEntity c, LocalDate hoje) {
        int meses = (int) ChronoUnit.MONTHS.between(c.getDataAdmissao(), hoje);
        int ativos = (int) mentoriaRepository.countByMentorIdAndStatusIn(c.getId(), ABERTAS);
        int pontos = pontuacaoService.totalDe(c.getId());
        boolean mesmaLoja = c.getLoja().getId().equals(novato.getLoja().getId());

        int score = (mesmaLoja ? 30 : 0)
                + Math.min(meses, 24)
                + Math.max(0, 20 - 10 * ativos)
                + Math.min(pontos / 10, 20);

        return new MentorSugestaoResponse(c.getId(), c.getColaboradorName(),
                c.getLoja().getId(), mesmaLoja, meses, ativos, pontos, score);
    }

    private ColaboradorEntity buscar(Long id) {
        return colaboradorRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Colaborador não encontrado"));
    }

    private MentoriaResponse toResponse(MentoriaEntity m) {
        return new MentoriaResponse(m.getId(),
                m.getMentor().getId(), m.getMentor().getColaboradorName(),
                m.getMentorado().getId(), m.getMentorado().getColaboradorName(),
                m.getStatus(), m.getCriadaEm());
    }
}