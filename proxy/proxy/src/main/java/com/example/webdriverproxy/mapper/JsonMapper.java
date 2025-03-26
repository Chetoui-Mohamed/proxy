package com.example.webdriverproxy.mapper;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.json.Json;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;

import static org.openqa.selenium.json.Json.MAP_TYPE;

/**
 * Un service qui fournit des fonctionnalités de mappage JSON pour les données de requêtes et réponses Selenium.
 * Cette classe utilise la bibliothèque Selenium {@link Json} pour convertir les chaînes JSON en structures de données
 * Java et extraire des informations spécifiques telles que les valeurs, les capacités ou les indicateurs d'erreur.
 */
@Slf4j(topic = "proxy")
@Service
public class JsonMapper {

    private static final String VALUE = "value";
    private static final String ERROR = "error";
    private static final String CAPABILITIES = "capabilities";

    private final Json json = new Json();

    /**
     * Convertit un corps de requête JSON en une carte de clés et valeurs.
     *
     * @param requestBody le corps de la requête sous forme de chaîne JSON
     * @return une carte représentant les données JSON analysées
     */
    public Map<String, Object> convertRequest(String requestBody) {
        return json.toType(requestBody, MAP_TYPE);
    }

    /**
     * Extrait la section "value" d'une réponse JSON.
     *
     * @param responseData les données de réponse sous forme de chaîne JSON
     * @return une carte contenant les données de la section "value", ou null si non présente
     */
    public Map<String, Object> getValue(String responseData) {
        Map<String, Map<String, Object>> result = json.toType(responseData, Json.MAP_TYPE);
        return result.get(VALUE);
    }

    /**
     * Récupère les capacités ("capabilities") à partir d'une carte de valeurs.
     *
     * @param value la carte contenant les données extraites de la section "value"
     * @return une carte des capacités, ou une carte vide si elles ne sont pas présentes
     */
    public Map<String, Object> getCapabilities(Map<String, Object> value) {
        return (Map<String, Object>) value.getOrDefault(CAPABILITIES, Collections.emptyMap());
    }

    /**
     * Vérifie si une réponse contient une erreur.
     *
     * @param responseData les données de réponse sous forme de chaîne JSON
     * @return true si la réponse contient une clé "error" dans la section "value", false sinon
     */
    public boolean isErrorResponse(String responseData) {
        Map<String, Object> valueMap = getValue(responseData);
        return valueMap != null && valueMap.containsKey(ERROR);
    }
}