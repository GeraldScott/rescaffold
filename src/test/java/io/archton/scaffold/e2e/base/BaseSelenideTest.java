package io.archton.scaffold.e2e.base;

import com.codeborne.selenide.Configuration;
import io.github.bonigarcia.wdm.WebDriverManager;
import io.quarkus.logging.Log;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import static com.codeborne.selenide.Selenide.closeWebDriver;

public abstract class BaseSelenideTest {
    
    protected static final String BASE_URL = "http://localhost:8080";
    
    @BeforeAll
    static void setUpAll() {
        // Setup WebDriver manager
        WebDriverManager.chromedriver().setup();
        
        // Configure Selenide
        Configuration.baseUrl = BASE_URL;
        Configuration.browser = "chrome";
        Configuration.browserSize = "1920x1080";
        Configuration.headless = true;
        Configuration.screenshots = true;
        Configuration.savePageSource = false;
        Configuration.timeout = 10000;
        Configuration.pageLoadTimeout = 30000;
        
        Log.info("Selenide configuration completed");
    }
    
    @BeforeEach
    void setUp() {
        Log.info("Starting test with base URL: " + BASE_URL);
    }
    
    @AfterAll
    static void tearDownAll() {
        closeWebDriver();
        Log.info("Selenide teardown completed");
    }
}