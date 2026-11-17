package com.futebol.futebol_analytics.infraestrutura.persistencia;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

interface PartidaJpaRepository extends JpaRepository<PartidaEntity, Long> {

    List<PartidaEntity> findByIdExternoIn(Collection<String> idsExternos);

    List<PartidaEntity> findByCodigoCompeticaoAndTemporadaOrderByDataHoraAscIdExternoAsc(
            String codigoCompeticao, int temporada);
}
