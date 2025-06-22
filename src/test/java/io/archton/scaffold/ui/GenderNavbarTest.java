package io.archton.scaffold.ui;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class GenderNavbarTest {

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeEach
    void setUp() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @DisplayName("Gender menu option should be present in navbar")
    void testGenderMenuOptionExists() {
        driver.get("http://localhost:8080");
        
        // Wait for page to load completely
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("nav")));
        
        // Check if the Gender menu item exists in the HTML (regardless of visibility)
        WebElement genderMenuItem = wait.until(
            ExpectedConditions.presenceOfElementLocated(
                By.xpath("//a[@class='dropdown-item' and @href='/genders-ui' and contains(text(), 'Genders')]")
            )
        );
        
        assertTrue(genderMenuItem != null, "Gender menu option should be present in the navbar HTML");
        
        // Additional verification: check the href attribute
        String href = genderMenuItem.getAttribute("href");
        assertTrue(href.endsWith("/genders-ui"), "Gender menu item should link to /genders-ui");
    }
}