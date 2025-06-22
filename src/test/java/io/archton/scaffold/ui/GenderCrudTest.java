package io.archton.scaffold.ui;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
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

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class GenderCrudTest {

    private static WebDriver driver;
    private static WebDriverWait wait;

    @BeforeAll
    static void setUp() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    static void tearDown() {
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

    @Test
    @DisplayName("Navigate to Gender page shows table with list of genders")
    void testGenderNavbarClickShowsTable() {
        driver.get("http://localhost:8080/genders-ui");
        
        // Wait for the genders table to appear
        wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//table[contains(@class, 'table')]")
        ));
        
        // Verify table headers
        WebElement codeHeader = driver.findElement(By.xpath("//th[contains(text(), 'Code')]"));
        WebElement descriptionHeader = driver.findElement(By.xpath("//th[contains(text(), 'Description')]"));
        WebElement actionsHeader = driver.findElement(By.xpath("//th[contains(text(), 'Actions')]"));
        
        assertNotNull(codeHeader, "Code column header should be present");
        assertNotNull(descriptionHeader, "Description column header should be present");
        assertNotNull(actionsHeader, "Actions column header should be present");
        
        // Verify Create button is present
        WebElement createButton = driver.findElement(
            By.xpath("//button[contains(@class, 'btn-outline-success')]")
        );
        assertNotNull(createButton, "Create button should be present");
    }

    @Test
    @DisplayName("Click Delete opens confirmation screen")
    void testDeleteConfirmationScreen() {
        driver.get("http://localhost:8080/genders-ui");
        
        // Wait for table to load and ensure there are rows
        wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//table[contains(@class, 'table')]")));
        wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//tbody/tr")));
        
        // Find first delete button and click it
        WebElement deleteButton = wait.until(
            ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(@class, 'btn-outline-danger')]")
            )
        );
        deleteButton.click();
        
        // Wait for delete confirmation screen
        WebElement deleteHeader = wait.until(
            ExpectedConditions.presenceOfElementLocated(
                By.xpath("//h5[contains(text(), 'Delete Gender')]")
            )
        );
        
        // Verify warning message
        WebElement warningMessage = driver.findElement(
            By.xpath("//div[contains(@class, 'alert-warning') and contains(text(), 'cannot be undone')]")
        );
        
        // Verify Cancel and Delete buttons
        WebElement cancelButton = driver.findElement(
            By.xpath("//button[contains(text(), 'Cancel')]")
        );
        WebElement confirmDeleteButton = driver.findElement(
            By.xpath("//button[contains(text(), 'Delete') and contains(@class, 'btn-danger')]")
        );
        
        assertNotNull(deleteHeader, "Delete confirmation header should be present");
        assertNotNull(warningMessage, "Warning message should be present");
        assertNotNull(cancelButton, "Cancel button should be present");
        assertNotNull(confirmDeleteButton, "Confirm delete button should be present");
    }

    @Test
    @DisplayName("Create form with invalid data shows error")
    void testCreateFormInvalidData() {
        driver.get("http://localhost:8080/genders-ui");
        
        // Wait for table and Create button to be available
        wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//table[contains(@class, 'table')]")));
        
        WebElement createButton = wait.until(
            ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(@class, 'btn-outline-success')]")
            )
        );
        createButton.click();
        
        // Wait for create form
        wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//h5[contains(text(), 'Create New Gender')]")
        ));
        
        // Try submitting with empty fields (invalid data)
        WebElement submitButton = driver.findElement(
            By.xpath("//button[@type='submit' and contains(@class, 'btn-success')]")
        );
        submitButton.click();
        
        // Wait briefly and verify we're still on create form (not redirected)
        try {
            Thread.sleep(1000); // Small delay to allow form validation
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        WebElement createHeader = driver.findElement(
            By.xpath("//h5[contains(text(), 'Create New Gender')]")
        );
        assertNotNull(createHeader, "Should still be on create form after invalid submission");
        
        // Check if browser validation is preventing submission
        WebElement codeInput = driver.findElement(By.id("code"));
        String validationMessage = codeInput.getAttribute("validationMessage");
        assertFalse(validationMessage == null || validationMessage.isEmpty(), 
                   "Code field should have validation message for required field");
    }

    @Test
    @DisplayName("Create form with valid data creates record")
    void testCreateFormValidData() {
        driver.get("http://localhost:8080/genders-ui");
        
        // Wait for table and Create button to be available
        wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//table[contains(@class, 'table')]")));
        
        WebElement createButton = wait.until(
            ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(@class, 'btn-outline-success')]")
            )
        );
        createButton.click();
        
        // Wait for create form
        wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//h5[contains(text(), 'Create New Gender')]")
        ));
        
        // Fill in valid data
        WebElement codeInput = driver.findElement(By.id("code"));
        WebElement descriptionInput = driver.findElement(By.id("description"));
        
        String testCode = "T";
        String testDescription = "Test Gender";
        
        codeInput.sendKeys(testCode);
        descriptionInput.sendKeys(testDescription);
        
        // Submit form
        WebElement submitButton = driver.findElement(
            By.xpath("//button[@type='submit' and contains(@class, 'btn-success')]")
        );
        submitButton.click();
        
        // Wait for redirect back to table
        wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//table[contains(@class, 'table')]")));
        
        // Verify the new record appears in table
        WebElement newRecord = wait.until(
            ExpectedConditions.presenceOfElementLocated(
                By.xpath("//td[contains(@class, 'fw-bold') and text()='" + testCode + "']")
            )
        );
        assertNotNull(newRecord, "New gender record should appear in table");
        
        // Verify description is also present
        WebElement descriptionCell = driver.findElement(
            By.xpath("//td[contains(@class, 'fw-bold') and text()='" + testCode + "']/following-sibling::td[text()='" + testDescription + "']")
        );
        assertNotNull(descriptionCell, "Description should match created record");
    }

    @Test
    @DisplayName("Edit form with invalid data shows error")
    void testEditFormInvalidData() {
        driver.get("http://localhost:8080/genders-ui");
        
        // Wait for table and ensure there are rows
        wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//table[contains(@class, 'table')]")));
        wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//tbody/tr")));
        
        WebElement editButton = wait.until(
            ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(@class, 'btn-outline-dark')]")
            )
        );
        editButton.click();
        
        // Wait for edit form
        wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//h5[contains(text(), 'Edit Gender')]")
        ));
        
        // Clear the required fields to make form invalid
        WebElement codeInput = driver.findElement(By.id("code"));
        WebElement descriptionInput = driver.findElement(By.id("description"));
        
        codeInput.clear();
        descriptionInput.clear();
        
        // Try to submit
        WebElement saveButton = driver.findElement(
            By.xpath("//button[@type='submit' and contains(@class, 'btn-primary')]")
        );
        saveButton.click();
        
        // Wait briefly and verify we're still on edit form
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        WebElement editHeader = driver.findElement(
            By.xpath("//h5[contains(text(), 'Edit Gender')]")
        );
        assertNotNull(editHeader, "Should still be on edit form after invalid submission");
        
        // Check validation message
        String validationMessage = codeInput.getAttribute("validationMessage");
        assertFalse(validationMessage == null || validationMessage.isEmpty(), 
                   "Code field should have validation message for required field");
    }

    @Test
    @DisplayName("Edit form with valid data updates record")
    void testEditFormValidData() {
        driver.get("http://localhost:8080/genders-ui");
        
        // Wait for table and ensure there are rows
        wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//table[contains(@class, 'table')]")));
        wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//tbody/tr")));
        
        // Get the original code and description
        WebElement firstCodeCell = driver.findElement(
            By.xpath("//tbody/tr[1]/td[contains(@class, 'fw-bold')]")
        );
        String originalCode = firstCodeCell.getText();
        
        // Click Edit button for first row
        WebElement editButton = wait.until(
            ExpectedConditions.elementToBeClickable(
                By.xpath("//tbody/tr[1]//button[contains(@class, 'btn-outline-dark')]")
            )
        );
        editButton.click();
        
        // Wait for edit form
        wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//h5[contains(text(), 'Edit Gender')]")
        ));
        
        // Update the description field
        WebElement descriptionInput = driver.findElement(By.id("description"));
        String originalDescription = descriptionInput.getAttribute("value");
        String newDescription = originalDescription + " (Updated)";
        
        descriptionInput.clear();
        descriptionInput.sendKeys(newDescription);
        
        // Submit form
        WebElement saveButton = driver.findElement(
            By.xpath("//button[@type='submit' and contains(@class, 'btn-primary')]")
        );
        saveButton.click();
        
        // Wait for redirect back to table
        wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//table[contains(@class, 'table')]")));
        
        // Verify the record was updated
        WebElement updatedDescription = wait.until(
            ExpectedConditions.presenceOfElementLocated(
                By.xpath("//td[contains(@class, 'fw-bold') and text()='" + originalCode + "']/following-sibling::td[contains(text(), '(Updated)')]")
            )
        );
        assertNotNull(updatedDescription, "Description should be updated in the table");
    }
}