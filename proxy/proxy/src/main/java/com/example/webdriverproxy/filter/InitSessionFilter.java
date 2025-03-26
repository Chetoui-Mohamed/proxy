package com.example.webdriverproxy.filter;

import com.example.webdriverproxy.model.SessionContext;
import com.example.webdriverproxy.service.SessionContextService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyResponseBodyGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Un filtre Spring Cloud Gateway responsable de l'initialisation des contextes de session pour les requêtes proxy Selenium WebDriver.
 * Ce filtre intercepte les requêtes de création de session, initialise un {@link SessionContext} à partir du corps de la requête fourni,
 * et modifie le corps de la réponse si nécessaire. Il est conçu pour s'exécuter avec la plus haute priorité parmi les filtres.
 */
@Component
@Slf4j(topic = "proxy")
public class InitSessionFilter extends AbstractGatewayFilterFactory<InitSessionFilter.Config> implements Ordered {

    private final ModifyResponseBodyGatewayFilterFactory modifyResponseBodyFilterFactory;
    private final SessionContextService sessionContextService;

    /**
     * Construit une instance de {@code InitSessionFilter} avec les dépendances requises.
     *
     * @param modifyResponseBodyFilterFactory la fabrique utilisée pour modifier les corps de réponse
     * @param sessionContextService           le service responsable de la gestion du contexte de session
     */
    public InitSessionFilter(ModifyResponseBodyGatewayFilterFactory modifyResponseBodyFilterFactory, SessionContextService sessionContextService) {
        super(Config.class);
        this.modifyResponseBodyFilterFactory = modifyResponseBodyFilterFactory;
        this.sessionContextService = sessionContextService;
    }

    /**
     * Crée et applique un {@link GatewayFilter} pour traiter l'initialisation de la session.
     * Cette méthode récupère le corps de la requête mis en cache, initialise le contexte de session, le soumet au service,
     * et renvoie le corps de réponse original. En cas d'erreur, elle définit un statut HTTP 500 et renvoie un message d'erreur.
     *
     * @param config l'objet de configuration pour ce filtre (actuellement inutilisé)
     * @return un {@link GatewayFilter} qui gère la logique d'initialisation de session
     */
    @Override
    public GatewayFilter apply(Config config) {
        final ModifyResponseBodyGatewayFilterFactory.Config modifyConfig = new ModifyResponseBodyGatewayFilterFactory.Config();
        modifyConfig.setRewriteFunction(String.class, String.class, (swe, responseBody) -> {
            String requestBody = swe.getAttribute("cachedRequestBody");
            log.info("Requête /wd/hub/session reçue avec le corps : {}", requestBody);
            try {
                SessionContext sessionContext = sessionContextService.initSessionContext(requestBody);
                log.info("Contexte de session initialisé : {}", sessionContext);
                sessionContextService.submitSessionContext(responseBody, sessionContext);
                log.info("Contexte de session soumis");
                return Mono.just(responseBody); // Renvoie la réponse originale de Selenium Grid
            } catch (Exception e) {
                log.error("Erreur dans InitSessionFilter : ", e);
                swe.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
                return Mono.just("{\"value\": {\"error\": \"session non créée\", \"message\": \"" + e.getMessage() + "\", \"stacktrace\": \"\"}}");
            }
        });
        return modifyResponseBodyFilterFactory.apply(modifyConfig);
    }

    /**
     * Définit l'ordre d'exécution de ce filtre, lui attribuant la plus haute priorité.
     *
     * @return la valeur de priorité, définie à {@link Ordered#HIGHEST_PRECEDENCE}
     */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    /**
     * Classe de configuration pour {@code InitSessionFilter}.
     * Cette classe est un espace réservé car aucune configuration supplémentaire n'est actuellement requise.
     */
    public static class Config {}
}