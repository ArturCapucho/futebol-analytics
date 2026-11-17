package com.futebol.futebol_analytics.infraestrutura.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Acesso à API football-data.org ({@code futebol.football-data.*}). */
@ConfigurationProperties(prefix = "futebol.football-data")
public record FootballDataProperties(String urlBase, String token) {
}
