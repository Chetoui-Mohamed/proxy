package com.example.webdriverproxy;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import java.net.URL;

/**
 * Une classe de test pour vérifier le fonctionnement du proxy Selenium WebDriver.
 * Cette classe établit une connexion au proxy, crée une session WebDriver, navigue vers une URL,
 * tente de localiser un élément inexistant pour tester la détection d'erreur, puis ferme le pilote.
 */
public class ProxyTest {

    /**
     * Méthode principale pour exécuter le test du proxy Selenium.
     * Configure une connexion au proxy sur le port spécifié, initialise une session Chrome,
     * effectue une navigation de test, simule une erreur et termine la session.
     *
     * @param args les arguments de la ligne de commande (non utilisés ici)
     */
    public static void main(String[] args) {
        try {
            URL proxyUrl = new URL("http://localhost:8086/wd/hub"); // Port du proxy
            DesiredCapabilities capabilities = new DesiredCapabilities();
            capabilities.setBrowserName("chrome");

            System.out.println("Connexion au proxy à : " + proxyUrl);
            WebDriver driver = new RemoteWebDriver(proxyUrl, capabilities);
            System.out.println("Session créée avec succès");

            driver.get("https://example.com");
            System.out.println("Navigation vers example.com effectuée");

            try {
                driver.findElement(By.xpath("//non-existent-element"));
            } catch (Exception e) {
                System.out.println("Erreur attendue capturée : " + e.getMessage());
            }

            driver.quit();
            System.out.println("Pilote fermé");
        } catch (Exception e) {
            System.err.println("Échec du test : " + e.getMessage());
            e.printStackTrace();
        }
    }
}