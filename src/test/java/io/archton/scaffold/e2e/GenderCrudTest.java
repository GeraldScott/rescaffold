package io.archton.scaffold.e2e;

import io.archton.scaffold.e2e.base.BaseSelenideTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selectors.*;
import static com.codeborne.selenide.Selenide.*;

@DisplayName("Gender CRUD E2E Tests")
class GenderCrudTest extends BaseSelenideTest {

    private String testCode;
    private String testDescription;
    private String updatedCode;
    private String updatedDescription;

    @BeforeEach
    void setUpTestData() {
        // Generate single character codes for Gender entity (maxlength=1)
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("ss"));
        char testChar = (char) ('A' + (Integer.parseInt(timestamp) % 26));
        char updatedChar = (char) ('A' + ((Integer.parseInt(timestamp) + 1) % 26));

        testCode = String.valueOf(testChar);
        testDescription = "Test Gender " + testChar;
        updatedCode = String.valueOf(updatedChar);
        updatedDescription = "Updated Test Gender " + updatedChar;

        // Navigate to the page once for all tests
        open("/genders-ui");
    }

    // Flag to track if we're in the create test
    private boolean isCreateTest = false;

    @AfterEach
    void cleanUpTestData() {
        System.out.println("[DEBUG_LOG] Running cleanup, isCreateTest: " + isCreateTest);

        // Skip cleanup for the create test
        if (isCreateTest) {
            System.out.println("[DEBUG_LOG] Skipping cleanup for create test");
            isCreateTest = false;
            return;
        }

        // Clean up any test data that might have been created
        refresh(); // Refresh to ensure clean state

        cleanupGenderIfExists(testCode);
        cleanupGenderIfExists(updatedCode);
    }

    private void cleanupGenderIfExists(String code) {
        try {
            System.out.println("[DEBUG_LOG] Cleaning up gender with code: " + code);
            // Use more direct selectors instead of page object methods
            if ($$("td.fw-bold").findBy(text(code)).exists()) {
                System.out.println("[DEBUG_LOG] Found gender with code: " + code);
                $$("td.fw-bold").findBy(text(code)).closest("tr")
                    .$("button[id^='delete-btn-']").click();
                System.out.println("[DEBUG_LOG] Clicked delete button");

                // Add a wait to give the server time to process the request
                try {
                    System.out.println("[DEBUG_LOG] Waiting for 1 second");
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }

                // Check if confirm-delete-btn exists before clicking
                if ($("#confirm-delete-btn").exists()) {
                    System.out.println("[DEBUG_LOG] Found confirm-delete-btn");
                    $("#confirm-delete-btn").click();
                    System.out.println("[DEBUG_LOG] Clicked confirm-delete-btn");
                } else {
                    System.out.println("[DEBUG_LOG] confirm-delete-btn not found, skipping");
                }
            } else {
                System.out.println("[DEBUG_LOG] Gender with code " + code + " not found");
            }
        } catch (Exception e) {
            // Ignore cleanup errors
            System.out.println("[DEBUG_LOG] Error during cleanup: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Should load Genders page and display table")
    void shouldLoadGendersPageAndDisplayTable() {
        // Use direct selectors with implicit waits
        $("h1").shouldHave(text("Genders"));
        $("#content-area").shouldBe(visible);
        $("table.table").shouldBe(visible);
        $("#create-new-btn").shouldBe(visible).shouldHave(text("Create"));
    }

    @Test
    @DisplayName("Should create new gender successfully")
    void shouldCreateNewGenderSuccessfully() {
        // Set flag to skip cleanup for this test
        isCreateTest = true;

        // Chain actions fluently
        System.out.println("[DEBUG_LOG] Test code: " + testCode);
        System.out.println("[DEBUG_LOG] Test description: " + testDescription);
        $("#create-new-btn").click();
        System.out.println("[DEBUG_LOG] Clicked create button");

        // Wait for form and fill it in one flow
        $("#code").shouldBe(visible).setValue(testCode);
        System.out.println("[DEBUG_LOG] Set code: " + testCode);
        $("#description").setValue(testDescription);
        System.out.println("[DEBUG_LOG] Set description: " + testDescription);

        // Submit and verify
        $("#submit-create-btn").click();
        System.out.println("[DEBUG_LOG] Clicked submit button");

        // Add a wait to give the server time to process the request
        try {
            System.out.println("[DEBUG_LOG] Waiting for 2 seconds");
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Verify record appears in table using more specific selector
        System.out.println("[DEBUG_LOG] Looking for td.fw-bold with text: " + testCode);
        $$("td.fw-bold").findBy(text(testCode)).shouldBe(visible);
        System.out.println("[DEBUG_LOG] Found td.fw-bold with text: " + testCode);

        // Add another wait to ensure the test completes before cleanup
        try {
            System.out.println("[DEBUG_LOG] Test completed successfully, waiting for 2 seconds before cleanup");
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    @DisplayName("Should view gender details successfully")
    void shouldViewGenderDetailsSuccessfully() {
        // Create test data
        createTestGender();

        // Click view button using text-based selection
        $$("td.fw-bold").findBy(text(testCode)).closest("tr")
            .$("button", 0).click(); // First button is View

        // Verify view page content with chained assertions
        $("#content-area")
            .shouldHave(text("Code:"))
            .shouldHave(text(testCode))
            .shouldHave(text("Description:"))
            .shouldHave(text(testDescription));

        // Go back
        $(byText("Back")).click();
        $("table.table").shouldBe(visible);
    }

    @Test
    @DisplayName("Should edit gender successfully")
    void shouldEditGenderSuccessfully() {
        createTestGender();

        // Click edit button (second button in row)
        $$("td.fw-bold").findBy(text(testCode)).closest("tr")
            .$("button", 1).click(); // Second button is Edit

        // Verify form loads with current values and update
        $("#code").shouldHave(value(testCode));
        $("#code").clear();
        $("#code").setValue(updatedCode);
        $("#description").shouldHave(value(testDescription));
        $("#description").clear();
        $("#description").setValue(updatedDescription);

        $("#submit-edit-btn").click();

        // Verify updated record
        $$("td.fw-bold").findBy(text(updatedCode)).shouldBe(visible);
        $$("td.fw-bold").findBy(text(testCode)).shouldNot(exist);
    }

    @Test
    @DisplayName("Should delete gender successfully")
    void shouldDeleteGenderSuccessfully() {
        createTestGender();

        // Click delete button (third button in row)
        $$("td.fw-bold").findBy(text(testCode)).closest("tr")
            .$("button", 2).click(); // Third button is Delete

        // Verify delete confirmation with chained assertions
        $("#content-area")
            .shouldHave(text("Delete Gender"))
            .shouldHave(text("You are about to delete this gender record"))
            .shouldHave(text(testCode));

        // Confirm deletion
        $("#confirm-delete-btn").shouldBe(visible).click();

        // Verify record is gone
        $("table.table").shouldBe(visible);
        $$("td.fw-bold").findBy(text(testCode)).shouldNot(exist);
    }

    @Test
    @DisplayName("Should toggle gender active status successfully")
    void shouldToggleGenderActiveStatusSuccessfully() {
        createTestGender();

        // Verify initially active
        var genderRow = $$("td.fw-bold").findBy(text(testCode)).closest("tr");
        genderRow.shouldHave(text("Active"));

        // Edit to make inactive
        genderRow.$("button", 1).click(); // Edit button

        $("#isActive").shouldBe(checked).click(); // Uncheck
        $("#submit-edit-btn").click();

        // Verify now inactive
        $$("td.fw-bold").findBy(text(testCode)).closest("tr")
            .shouldHave(text("Inactive"));

        // Edit to make active again
        $$("td.fw-bold").findBy(text(testCode)).closest("tr")
            .$("button", 1).click(); // Edit button

        $("#isActive").shouldNotBe(checked).click(); // Check
        $("#submit-edit-btn").click();

        // Verify active again
        $$("td.fw-bold").findBy(text(testCode)).closest("tr")
            .shouldHave(text("Active"));
    }

    @Test
    @DisplayName("Should cancel operations without side effects")
    void shouldCancelOperationsWithoutSideEffects() {
        // Test cancel create
        $("#create-new-btn").click();
        $("#code").setValue("CANCEL");
        $("#cancel-create-btn").click();

        $("table.table").shouldBe(visible);
        $$("td.fw-bold").findBy(text("CANCEL")).shouldNot(exist);

        // Test cancel edit
        createTestGender();
        $$("td.fw-bold").findBy(text(testCode)).closest("tr")
            .$("button", 1).click(); // Edit

        $("#code").clear();
        $("#code").setValue("EDITED");
        $("#cancel-edit-btn").click();

        // Verify original data unchanged
        $$("td.fw-bold").findBy(text(testCode)).shouldBe(visible);
        $$("td.fw-bold").findBy(text("EDITED")).shouldNot(exist);

        // Test cancel delete
        $$("td.fw-bold").findBy(text(testCode)).closest("tr")
            .$("button", 2).click(); // Delete

        $(byText("Cancel")).click();

        // Verify record still exists
        $$("td.fw-bold").findBy(text(testCode)).shouldBe(visible);
    }

    private void createTestGender() {
        $("#create-new-btn").click();
        System.out.println("[DEBUG_LOG] Clicking create button");
        $("#code").setValue(testCode);
        System.out.println("[DEBUG_LOG] Setting code: " + testCode);
        $("#description").setValue(testDescription);
        System.out.println("[DEBUG_LOG] Setting description: " + testDescription);
        $("#submit-create-btn").click();
        System.out.println("[DEBUG_LOG] Clicking submit button");

        // Wait for table to load
        System.out.println("[DEBUG_LOG] Waiting for table to load with code: " + testCode);
        $$("td.fw-bold").findBy(text(testCode)).shouldBe(visible);
        System.out.println("[DEBUG_LOG] Found table row with code: " + testCode);
    }
}
