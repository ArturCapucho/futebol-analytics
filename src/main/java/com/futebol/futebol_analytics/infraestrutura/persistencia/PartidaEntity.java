package com.futebol.futebol_analytics.infraestrutura.persistencia;

import com.futebol.futebol_analytics.dominio.Competicao;
import com.futebol.futebol_analytics.dominio.Partida;
import com.futebol.futebol_analytics.dominio.Placar;
import com.futebol.futebol_analytics.dominio.StatusPartida;
import com.futebol.futebol_analytics.dominio.Time;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * Linha da tabela {@code partida}. Detalhe de persistência: o resto da
 * aplicação só enxerga {@link Partida}.
 */
@Entity
@Table(name = "partida")
class PartidaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_externo", nullable = false, unique = true)
    private String idExterno;

    @Column(name = "codigo_competicao", nullable = false)
    private String codigoCompeticao;

    @Column(nullable = false)
    private int temporada;

    @Column(nullable = false)
    private int rodada;

    @Column(name = "data_hora", nullable = false)
    private Instant dataHora;

    @Column(nullable = false)
    private String mandante;

    @Column(nullable = false)
    private String visitante;

    @Column(nullable = false)
    private String status;

    @Column(name = "gols_mandante")
    private Integer golsMandante;

    @Column(name = "gols_visitante")
    private Integer golsVisitante;

    protected PartidaEntity() {
        // exigido pelo JPA
    }

    static PartidaEntity de(Partida partida) {
        PartidaEntity entity = new PartidaEntity();
        entity.idExterno = partida.idExterno();
        entity.atualizarCom(partida);
        return entity;
    }

    void atualizarCom(Partida partida) {
        codigoCompeticao = partida.competicao().codigo();
        temporada = partida.competicao().temporada();
        rodada = partida.rodada();
        dataHora = partida.dataHora();
        mandante = partida.mandante().nome();
        visitante = partida.visitante().nome();
        status = partida.status().name();
        Placar placar = partida.placar();
        golsMandante = placar == null ? null : placar.golsMandante();
        golsVisitante = placar == null ? null : placar.golsVisitante();
    }

    Partida paraDominio() {
        Placar placar = golsMandante == null || golsVisitante == null
                ? null
                : new Placar(golsMandante, golsVisitante);
        return new Partida(
                idExterno,
                new Competicao(codigoCompeticao, temporada),
                rodada,
                dataHora,
                new Time(mandante),
                new Time(visitante),
                StatusPartida.valueOf(status),
                placar);
    }

    String getIdExterno() {
        return idExterno;
    }
}
