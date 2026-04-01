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
        page.setDefaultTimeout(60000);
        page.setDefaultNavigationTimeout(60000);
        page.navigate("http://localhost:" + port + "/", new Page.NavigateOptions().setTimeout(60000));

        String title = page.title();
        assertTrue(title.contains("Dashboard"), "A página principal deve ter o título Dashboard");

        boolean isLogoPresent = page.locator(".page-title").isVisible();
        assertTrue(isLogoPresent, "O título na página deve estar visível");

        page.close();
    }

    @Test
    void testTabsNavigation() {
        Page page = browser.newPage();
        page.setDefaultTimeout(60000);
        page.setDefaultNavigationTimeout(60000);
        page.navigate("http://localhost:" + port + "/", new Page.NavigateOptions().setTimeout(60000));

        // Verifica navegação para Rede
        page.locator("text=Rede").click();
        page.waitForURL("**/rede", new Page.WaitForURLOptions().setTimeout(60000));
        page.waitForSelector("h1.page-title", new Page.WaitForSelectorOptions().setTimeout(60000));
        assertTrue(page.locator("h1.page-title").textContent().contains("Monitoramento de Rede"), "Deve navegar para aba Rede");

        page.close();
    }
}
