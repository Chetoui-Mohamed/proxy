package com.example.webdriverproxy.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

/**
 * Un filtre Spring Cloud Gateway qui détecte et journalise les erreurs dans les réponses des requêtes proxy.
 * Ce filtre analyse les réponses pour identifier les codes d'état d'erreur, puis vérifie si elles contiennent des types
 * d'erreurs spécifiques prédéfinis tels que "no such element", "stale element reference" ou "timeout".
 */
@Slf4j(topic = "proxy")
@Component
public class ErrorDetectionFilter extends AbstractGatewayFilterFactory<ErrorDetectionFilter.Config> {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final List<String> detectableErrors = Arrays.asList(
            "no such element",
            "stale element reference",
            "timeout"
    );

    /**
     * Construit une instance de {@code ErrorDetectionFilter}.
     * Initialise le filtre avec la classe de configuration spécifiée.
     */
    public ErrorDetectionFilter() {
        super(Config.class);
    }

    /**
     * Crée et applique un {@link GatewayFilter} pour détecter les erreurs dans les réponses.
     * Ce filtre vérifie le code d'état de la réponse après l'exécution de la chaîne de filtres. En cas d'erreur,
     * il analyse le corps de la réponse pour identifier les types d'erreurs spécifiques et les journalise.
     *
     * @param config l'objet de configuration pour ce filtre (actuellement inutilisé)
     * @return un {@link GatewayFilter} qui gère la détection des erreurs
     */
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                ServerHttpResponse response = exchange.getResponse();
                if (response.getStatusCode() != null && response.getStatusCode().isError()) {
                    String responseBody = exchange.getAttribute("cachedResponseBody");
                    String requestBody = exchange.getAttribute("cachedRequestBody");
                    log.error("Erreur détectée : Statut={}, Corps de la requête={}, Corps de la réponse={}",
                            response.getStatusCode().value(), requestBody, responseBody);
                    if (responseBody != null && requestBody != null) {
                        try {
                            JsonNode jsonNode = objectMapper.readTree(responseBody);
                            JsonNode errorNode = jsonNode.path("value").path("error");
                            if (!errorNode.isMissingNode()) {
                                String errorType = errorNode.asText();
                                if (detectableErrors.contains(errorType)) {
                                    log.error("Erreur spécifique détectée : Type={}, Localisateur dans la requête={}",
                                            errorType, requestBody);
                                } else {
                                    log.error("Type d'erreur inconnu : {}", errorType);
                                }
                            }
                        } catch (Exception e) {
                            log.error("Échec de l'analyse du corps de la réponse : {}", e.getMessage());
                        }
                    }
                }
            }));
        };
    }

    /**
     * Classe de configuration pour {@code ErrorDetectionFilter}.
     * Cette classe est un espace réservé car aucune configuration supplémentaire n'est actuellement requise.
     */
    public static class Config {}
}