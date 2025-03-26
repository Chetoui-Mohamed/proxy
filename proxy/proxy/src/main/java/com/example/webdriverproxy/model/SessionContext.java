package com.example.webdriverproxy.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.net.URL;
import java.util.Map;

/**
 * Une classe modèle représentant le contexte d'une session pour le proxy Selenium WebDriver.
 * Cette classe stocke des informations clés telles que les capacités, l'URL du serveur Selenium,
 * l'identifiant de session et le corps de la requête de création de session.
 */
@Data
@Accessors(chain = true)
public class SessionContext {

    /** Les capacités (capabilities) de la session, sous forme de carte clé-valeur. */
    private Map<String, Object> capabilities;

    /** L'URL du serveur Selenium associé à la session. */
    private URL url;

    /** L'identifiant unique de la session. */
    private String sessionId;

    /** Le corps de la requête utilisé pour créer la session. */
    private String createSessionReqBody;
}