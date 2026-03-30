package com.monitora.ui;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DashboardUITest {

    @LocalServerPort
    private int port;

    private static Playwright playwright;
    private static Browser browser;

    @BeforeAll
    static void setUp() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
    }

    @AfterAll
    static void tearDown() {
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
    }

    @Test
    void testDashboardTitle() {
        Page page = browser.newPage();
        page.navigate("http://localhost:" + port + "/");
        
        String title = page.title();
        assertTrue(title.contains("Dashboard"), "A página principal deve ter o título Dashboard");
        
        boolean isLogoPresent = page.locator(".header-logo").isVisible();
        assertTrue(isLogoPresent, "A logo na barra superior deve estar visível");
        
        page.close();
    }
    
    @Test
    void testTabsNavigation() {
        Page page = browser.newPage();
        page.navigate("http://localhost:" + port + "/");
        
        // Verifica navegação para Rede
        page.locator("text=Rede").click();
        page.waitForURL("**/rede");
        assertTrue(page.title().contains("Rede"), "Deve navegar para aba Rede");
        
        page.close();
    }
}
