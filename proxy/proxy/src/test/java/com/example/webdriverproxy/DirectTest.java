package com.example.webdriverproxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import java.net.URL;

import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import java.net.URL;

public class DirectTest {
    public static void main(String[] args) throws Exception {
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setBrowserName("chrome");
        RemoteWebDriver driver = new RemoteWebDriver(new URL("http://localhost/wd/hub"), capabilities);
        System.out.println("Session ID: " + driver.getSessionId());
        driver.quit();
    }
}