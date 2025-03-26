package com.example.webdriverproxy.filter;

import org.springframework.cloud.gateway.filter.factory.rewrite.RewriteFunction;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Une implémentation de {@link RewriteFunction} qui met en cache le corps de la réponse dans les attributs de l'échange.
 * Cette classe est utilisée dans un filtre Spring Cloud Gateway pour stocker le corps de la réponse afin qu'il puisse
 * être réutilisé par d'autres filtres dans la chaîne de traitement.
 */
@Component
public class CacheResponseBodyRewrite implements RewriteFunction<String, String> {

    /**
     * Met en cache le corps de la réponse et le renvoie sans modification.
     * Cette méthode stocke le corps de la réponse dans les attributs de l'échange sous la clé "cachedResponseBody",
     * puis renvoie le corps original encapsulé dans un {@link Mono}.
     *
     * @param exchange l'objet {@link ServerWebExchange} représentant le contexte de la requête/réponse
     * @param body     le corps de la réponse sous forme de chaîne de caractères
     * @return un {@link Mono} contenant le corps de la réponse inchangé
     */
    @Override
    public Mono<String> apply(ServerWebExchange exchange, String body) {
        exchange.getAttributes().put("cachedResponseBody", body);
        return Mono.just(body);
    }
}