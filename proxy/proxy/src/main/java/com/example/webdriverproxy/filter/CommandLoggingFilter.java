package com.example.webdriverproxy.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Un filtre Spring Cloud Gateway qui journalise les détails des commandes interceptées dans les requêtes proxy.
 * Ce filtre capture et enregistre le chemin, la méthode HTTP et le corps de la requête (si disponible) pour chaque
 * commande interceptée, facilitant le suivi et le débogage.
 */
@Slf4j(topic = "proxy")
@Component
public class CommandLoggingFilter extends AbstractGatewayFilterFactory<CommandLoggingFilter.Config> {

    /**
     * Construit une instance de {@code CommandLoggingFilter}.
     * Initialise le filtre avec la classe de configuration spécifiée.
     */
    public CommandLoggingFilter() {
        super(Config.class);
    }

    /**
     * Crée et applique un {@link GatewayFilter} pour journaliser les commandes interceptées.
     * Ce filtre extrait les informations de la requête (chemin, méthode et corps mis en cache) et les enregistre
     * avant de passer la requête à la chaîne de filtres suivante.
     *
     * @param config l'objet de configuration pour ce filtre (actuellement inutilisé)
     * @return un {@link GatewayFilter} qui gère la journalisation des commandes
     */
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String cachedBody = exchange.getAttribute("cachedRequestBody");
            log.info("Commande interceptée : Chemin={}, Méthode={}, Corps={}",
                    request.getPath().value(),
                    request.getMethod().toString(),
                    cachedBody != null ? cachedBody : "Pas de corps");
            return chain.filter(exchange);
        };
    }

    /**
     * Classe de configuration pour {@code CommandLoggingFilter}.
     * Cette classe est un espace réservé car aucune configuration supplémentaire n'est actuellement requise.
     */
    public static class Config {}
}