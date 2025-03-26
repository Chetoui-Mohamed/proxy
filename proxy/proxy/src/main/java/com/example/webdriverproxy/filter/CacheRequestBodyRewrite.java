package com.example.webdriverproxy.filter;

import org.springframework.cloud.gateway.filter.factory.rewrite.RewriteFunction;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Une implémentation de {@link RewriteFunction} qui met en cache le corps de la requête dans les attributs de l'échange.
 * Cette classe est utilisée dans un filtre Spring Cloud Gateway pour stocker le corps de la requête afin qu'il puisse
 * être réutilisé par d'autres filtres dans la chaîne de traitement.
 */
@Component
public class CacheRequestBodyRewrite implements RewriteFunction<String, String> {

    /**
     * Met en cache le corps de la requête et le renvoie sans modification.
     * Cette méthode stocke le corps de la requête dans les attributs de l'échange sous la clé "cachedRequestBody",
     * puis renvoie le corps original encapsulé dans un {@link Mono}.
     *
     * @param exchange l'objet {@link ServerWebExchange} représentant le contexte de la requête/réponse
     * @param body     le corps de la requête sous forme de chaîne de caractères
     * @return un {@link Mono} contenant le corps de la requête inchangé
     */
    @Override
    public Mono<String> apply(ServerWebExchange exchange, String body) {
        exchange.getAttributes().put("cachedRequestBody", body);
        return Mono.just(body);
    }
}