package com.futebol.futebol_analytics.infraestrutura.persistencia;

import com.futebol.futebol_analytics.aplicacao.porta.RepositorioDePartidas;
import com.futebol.futebol_analytics.dominio.Competicao;
import com.futebol.futebol_analytics.dominio.Partida;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/** Adaptador da porta {@link RepositorioDePartidas} sobre Spring Data JPA. */
@Repository
class RepositorioDePartidasJpa implements RepositorioDePartidas {

    private final PartidaJpaRepository jpa;

    RepositorioDePartidasJpa(PartidaJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    @Transactional
    public void salvarTodas(Collection<Partida> partidas) {
        if (partidas.isEmpty()) {
            return;
        }
        List<String> idsExternos = partidas.stream().map(Partida::idExterno).toList();
        Map<String, PartidaEntity> existentes = jpa.findByIdExternoIn(idsExternos).stream()
                .collect(Collectors.toMap(PartidaEntity::getIdExterno, Function.identity()));

        List<PartidaEntity> paraSalvar = new ArrayList<>(partidas.size());
        for (Partida partida : partidas) {
            PartidaEntity entity = existentes.get(partida.idExterno());
            if (entity == null) {
                entity = PartidaEntity.de(partida);
                existentes.put(partida.idExterno(), entity);
            } else {
                entity.atualizarCom(partida);
            }
            paraSalvar.add(entity);
        }
        jpa.saveAll(paraSalvar);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Partida> buscarPor(Competicao competicao) {
        return jpa.findByCodigoCompeticaoAndTemporadaOrderByDataHoraAscIdExternoAsc(
                        competicao.codigo(), competicao.temporada())
                .stream()
                .map(PartidaEntity::paraDominio)
                .toList();
    }
}
