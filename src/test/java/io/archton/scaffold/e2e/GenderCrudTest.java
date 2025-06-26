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
        // Generate unique test data for each test run
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("ss"));
        char testChar = (char) ('A' + (Integer.parseInt(timestamp) % 26));
        char updatedChar = (char) ('A' + ((Integer.parseInt(timestamp) + 1) % 26));

        testCode = String.valueOf(testChar);
        testDescription = "Test Gender " + testChar;
        updatedCode = String.valueOf(updatedChar);
        updatedDescription = "Updated Test Gender " + updatedChar;

        // Navigate to the page
        open("/genders-ui");
    }

    @AfterEach
    void cleanUp() {
        // Simple cleanup: delete any test data that was created
        // This is necessary for E2E tests since @TestTransaction doesn't work across HTTP
        try {
            refresh(); // Ensure clean state
            deleteGenderIfExists(testCode);
            deleteGenderIfExists(updatedCode);
        } catch (Exception e) {
            // Log but don't fail the test due to cleanup issues
            System.err.println("Cleanup warning: " + e.getMessage());
        }
    }

    private void deleteGenderIfExists(String code) {
        try {
            refresh(); // Ensure we have the latest state
            
            if ($$("td.fw-bold").findBy(text(code)).exists()) {
                // Scroll to element and click delete
                var deleteButton = $$("td.fw-bold").findBy(text(code)).closest("tr")
                    .$("button[id^='delete-btn-']");
                deleteButton.scrollTo().click();

                // Wait for delete confirmation dialog to appear
                $("#confirm-delete-btn").shouldBe(visible, java.time.Duration.ofSeconds(5));
                
                // Click confirm delete
                $("#confirm-delete-btn").click();

                // Wait for deletion to complete - check the table is back and element is gone
                $("table.table").shouldBe(visible, java.time.Duration.ofSeconds(10));
                $$("td.fw-bold").findBy(text(code)).should(not(exist), java.time.Duration.ofSeconds(5));
            }
        } catch (Exception e) {
            // Ignore cleanup errors to prevent test failures
            System.err.println("Cleanup warning for " + code + ": " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Should load Genders page and display table")
    void shouldLoadGendersPageAndDisplayTable() {
        $("h1").shouldHave(text("Genders"));
        $("#content-area").shouldBe(visible);
        $("table.table").shouldBe(visible);
        $("#create-new-btn").shouldBe(visible).shouldHave(text("Create"));
    }

    @Test
    @DisplayName("Should create new gender successfully")
    void shouldCreateNewGenderSuccessfully() {
        $("#create-new-btn").scrollTo().click();

        // Wait for form to appear
        $("#code").shouldBe(visible, java.time.Duration.ofSeconds(5));
        $("#code").setValue(testCode);
        $("#description").setValue(testDescription);
        $("#submit-create-btn").click();

        // Wait for table to reappear and verify record appears
        $("table.table").shouldBe(visible, java.time.Duration.ofSeconds(10));
        $$("td.fw-bold").findBy(text(testCode)).shouldBe(visible, java.time.Duration.ofSeconds(5));
    }

    @Test
    @DisplayName("Should view gender details successfully")
    void shouldViewGenderDetailsSuccessfully() {
        createTestGender();

        // Click view button (first button in row)
        var viewButton = $$("td.fw-bold").findBy(text(testCode)).closest("tr")
            .$("button", 0);
        viewButton.scrollTo().click();

        // Wait for view details to appear and verify content
        $("#content-area").shouldBe(visible, java.time.Duration.ofSeconds(5));
        $("#content-area")
            .shouldHave(text("Code:"), java.time.Duration.ofSeconds(5))
            .shouldHave(text(testCode))
            .shouldHave(text("Description:"))
            .shouldHave(text(testDescription));

        // Go back
        $(byText("Back")).click();
        $("table.table").shouldBe(visible, java.time.Duration.ofSeconds(10));
    }

    @Test
    @DisplayName("Should edit gender successfully")
    void shouldEditGenderSuccessfully() {
        createTestGender();

        // Click edit button (second button in row)
        var editButton = $$("td.fw-bold").findBy(text(testCode)).closest("tr")
            .$("button", 1);
        editButton.scrollTo().click();

        // Wait for edit form to appear and update it
        $("#code").shouldBe(visible, java.time.Duration.ofSeconds(5))
            .shouldHave(value(testCode));
        $("#code").clear();
        $("#code").setValue(updatedCode);

        $("#description").shouldHave(value(testDescription));
        $("#description").clear();
        $("#description").setValue(updatedDescription);
        $("#submit-edit-btn").click();

        // Wait for table to reappear and verify updated record
        $("table.table").shouldBe(visible, java.time.Duration.ofSeconds(10));
        $$("td.fw-bold").findBy(text(updatedCode)).shouldBe(visible, java.time.Duration.ofSeconds(5));
        $$("td.fw-bold").findBy(text(testCode)).should(not(exist));
    }

    @Test
    @DisplayName("Should delete gender successfully")
    void shouldDeleteGenderSuccessfully() {
        createTestGender();

        // Click delete button (third button in row)
        var deleteButton = $$("td.fw-bold").findBy(text(testCode)).closest("tr")
            .$("button", 2);
        deleteButton.scrollTo().click();

        // Wait for delete confirmation to appear and verify content
        $("#content-area").shouldBe(visible, java.time.Duration.ofSeconds(5));
        $("#content-area")
            .shouldHave(text("Delete Gender"), java.time.Duration.ofSeconds(5))
            .shouldHave(text("You are about to delete this gender record"))
            .shouldHave(text(testCode));

        // Confirm deletion
        $("#confirm-delete-btn").shouldBe(visible, java.time.Duration.ofSeconds(5)).click();

        // Wait for table to reappear and verify record is gone
        $("table.table").shouldBe(visible, java.time.Duration.ofSeconds(10));
        $$("td.fw-bold").findBy(text(testCode)).should(not(exist), java.time.Duration.ofSeconds(5));
    }

    @Test
    @DisplayName("Should toggle gender active status successfully")
    void shouldToggleGenderActiveStatusSuccessfully() {
        createTestGender();

        var genderRow = $$("td.fw-bold").findBy(text(testCode)).closest("tr");
        genderRow.shouldHave(text("Active"));

        // Edit to make inactive
        genderRow.$("button", 1).scrollTo().click();
        
        // Wait for edit form and toggle active status
        $("#isActive").shouldBe(visible, java.time.Duration.ofSeconds(5))
            .shouldBe(checked).click(); // Uncheck
        $("#submit-edit-btn").click();

        // Wait for table and verify now inactive
        $("table.table").shouldBe(visible, java.time.Duration.ofSeconds(10));
        $$("td.fw-bold").findBy(text(testCode)).closest("tr")
            .shouldHave(text("Inactive"), java.time.Duration.ofSeconds(5));

        // Edit to make active again
        $$("td.fw-bold").findBy(text(testCode)).closest("tr")
            .$("button", 1).scrollTo().click();
        
        // Wait for edit form and toggle active status back
        $("#isActive").shouldBe(visible, java.time.Duration.ofSeconds(5))
            .shouldNotBe(checked).click(); // Check
        $("#submit-edit-btn").click();

        // Wait for table and verify active again
        $("table.table").shouldBe(visible, java.time.Duration.ofSeconds(10));
        $$("td.fw-bold").findBy(text(testCode)).closest("tr")
            .shouldHave(text("Active"), java.time.Duration.ofSeconds(5));
    }

    @Test
    @DisplayName("Should cancel operations without side effects")
    void shouldCancelOperationsWithoutSideEffects() {
        // Test cancel create
        $("#create-new-btn").scrollTo().click();
        
        // Wait for create form and fill it
        $("#code").shouldBe(visible, java.time.Duration.ofSeconds(5))
            .setValue("Z"); // Use a different code to avoid conflicts
        $("#cancel-create-btn").click();

        // Verify back to table and no record created
        $("table.table").shouldBe(visible, java.time.Duration.ofSeconds(10));
        $$("td.fw-bold").findBy(text("Z")).shouldNot(exist);

        // Test cancel edit with existing data
        createTestGender();
        var editButton = $$("td.fw-bold").findBy(text(testCode)).closest("tr")
            .$("button", 1);
        editButton.scrollTo().click();

        // Wait for edit form and modify it
        $("#code").shouldBe(visible, java.time.Duration.ofSeconds(5));
        $("#code").clear();
        $("#code").setValue("Y");
        $("#cancel-edit-btn").click();

        // Verify back to table and original data unchanged
        $("table.table").shouldBe(visible, java.time.Duration.ofSeconds(10));
        $$("td.fw-bold").findBy(text(testCode)).shouldBe(visible);
        $$("td.fw-bold").findBy(text("Y")).shouldNot(exist);

        // Test cancel delete
        var deleteButton = $$("td.fw-bold").findBy(text(testCode)).closest("tr")
            .$("button", 2);
        deleteButton.scrollTo().click();

        // Wait for delete confirmation and cancel
        $("#cancel-delete-btn").shouldBe(visible, java.time.Duration.ofSeconds(5)).click();

        // Verify back to table and record still exists
        $("table.table").shouldBe(visible, java.time.Duration.ofSeconds(10));
        $$("td.fw-bold").findBy(text(testCode)).shouldBe(visible);
    }

    private void createTestGender() {
        $("#create-new-btn").scrollTo().click();
        
        // Wait for create form to appear
        $("#code").shouldBe(visible, java.time.Duration.ofSeconds(5));
        $("#code").setValue(testCode);
        $("#description").setValue(testDescription);
        $("#submit-create-btn").click();

        // Wait for table to reappear and record to be created
        $("table.table").shouldBe(visible, java.time.Duration.ofSeconds(10));
        $$("td.fw-bold").findBy(text(testCode)).shouldBe(visible, java.time.Duration.ofSeconds(5));
    }
}