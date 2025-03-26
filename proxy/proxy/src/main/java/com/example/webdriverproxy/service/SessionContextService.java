package com.example.webdriverproxy.service;

import com.example.webdriverproxy.mapper.JsonMapper;
import com.example.webdriverproxy.model.SessionContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.map.PassiveExpiringMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Un service qui gère le contexte des sessions pour le proxy Selenium WebDriver.
 * Ce service initialise, enrichit et stocke les contextes de session dans un cache à expiration automatique,
 * tout en interagissant avec {@link JsonMapper} pour traiter les données JSON des requêtes et réponses.
 */
@Slf4j(topic = "proxy")
@Service
public class SessionContextService {

    @Value("${proxy.selenium.url}")
    private String seleniumUrl;

    private PassiveExpiringMap<String, SessionContext> sessionContextCache = new PassiveExpiringMap<>(8, TimeUnit.HOURS);

    private final JsonMapper jsonMapper;

    /**
     * Construit une instance de {@code SessionContextService} avec le mapper JSON requis.
     *
     * @param jsonMapper le service de mappage JSON utilisé pour analyser les données
     */
    public SessionContextService(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    /**
     * Initialise un nouveau contexte de session à partir d'une requête.
     * Crée un contexte par défaut avec l'URL Selenium et y associe le corps de la requête.
     *
     * @param request le corps de la requête de création de session
     * @return le contexte de session initialisé
     */
    public SessionContext initSessionContext(String request) {
        SessionContext sessionContext = getDefaultSessionContext(seleniumUrl);
        log.info("[Proxy] Utilisation du serveur Selenium : {}", seleniumUrl);
        return sessionContext.setCreateSessionReqBody(request);
    }

    /**
     * Crée un contexte de session par défaut avec une URL Selenium spécifiée.
     *
     * @param urlStr l'URL du serveur Selenium sous forme de chaîne
     * @return un nouveau contexte de session avec l'URL définie
     * @throws RuntimeException si l'URL est invalide
     */
    private SessionContext getDefaultSessionContext(String urlStr) {
        URL url;
        try {
            url = new URL(urlStr);
        } catch (MalformedURLException e) {
            log.error("[Proxy] Erreur lors de la création de l'URL du serveur Selenium : {}", e.getMessage());
            throw new RuntimeException("URL Selenium invalide : " + urlStr, e);
        }
        return new SessionContext().setUrl(url);
    }

    /**
     * Soumet un contexte de session au cache et retourne son identifiant.
     * Enrichit le contexte avec les données de la réponse avant de le stocker.
     *
     * @param responseData les données de la réponse JSON contenant les détails de la session
     * @param sessionContext le contexte de session à soumettre
     * @return l'identifiant de la session
     */
    public String submitSessionContext(String responseData, SessionContext sessionContext) {
        String sessionId = enrichSessionContext(responseData, sessionContext);
        sessionContextCache.put(sessionId, sessionContext);
        return sessionId;
    }

    /**
     * Enrichit un contexte de session avec les données extraites d'une réponse JSON.
     * Définit les capacités et l'identifiant de session dans le contexte.
     *
     * @param responseData les données de la réponse JSON
     * @param sessionContext le contexte de session à enrichir
     * @return l'identifiant de la session extrait de la réponse
     * @throws RuntimeException si les données de réponse sont invalides ou si l'identifiant est absent
     */
    public String enrichSessionContext(String responseData, SessionContext sessionContext) {
        Map<String, Object> value = jsonMapper.getValue(responseData);
        if (value == null) {
            throw new RuntimeException("Données de réponse invalides : " + responseData);
        }
        sessionContext.setCapabilities(jsonMapper.getCapabilities(value));
        String sessionId = (String) value.get("sessionId");
        if (sessionId == null) {
            throw new RuntimeException("Identifiant de session non trouvé dans la réponse : " + responseData);
        }
        sessionContext.setSessionId(sessionId);
        return sessionId;
    }

    /**
     * Récupère un contexte de session à partir du cache en utilisant son identifiant.
     *
     * @param currentSessionId l'identifiant de la session à récupérer
     * @return le contexte de session correspondant
     * @throws RuntimeException si le contexte de session n'est pas trouvé dans le cache
     */
    public SessionContext getSessionContext(String currentSessionId) {
        SessionContext sessionContext = sessionContextCache.get(currentSessionId);
        if (sessionContext == null) {
            throw new RuntimeException("[Proxy] Contexte de session non trouvé : " + currentSessionId);
        }
        return sessionContext;
    }
}