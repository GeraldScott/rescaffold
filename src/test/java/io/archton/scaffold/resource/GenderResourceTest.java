package io.archton.scaffold.resource;

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
        if ($$("td.fw-bold").findBy(text(code)).exists()) {
            $$("td.fw-bold").findBy(text(code)).closest("tr")
                .$("button[id^='delete-btn-']").click();

            // Wait for delete confirmation dialog
            $("#confirm-delete-btn").shouldBe(visible).click();

            // Wait for deletion to complete
            $$("td.fw-bold").findBy(text(code)).shouldNot(exist);
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
        $("#create-new-btn").click();

        $("#code").shouldBe(visible).setValue(testCode);
        $("#description").setValue(testDescription);
        $("#submit-create-btn").click();

        // Verify record appears in table
        $$("td.fw-bold").findBy(text(testCode)).shouldBe(visible);
    }

    @Test
    @DisplayName("Should view gender details successfully")
    void shouldViewGenderDetailsSuccessfully() {
        createTestGender();

        // Click view button (first button in row)
        $$("td.fw-bold").findBy(text(testCode)).closest("tr")
            .$("button", 0).click();

        // Verify view page content
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
            .$("button", 1).click();

        // Update the form
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
            .$("button", 2).click();

        // Verify delete confirmation
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

        var genderRow = $$("td.fw-bold").findBy(text(testCode)).closest("tr");
        genderRow.shouldHave(text("Active"));

        // Edit to make inactive
        genderRow.$("button", 1).click();
        $("#isActive").shouldBe(checked).click(); // Uncheck
        $("#submit-edit-btn").click();

        // Verify now inactive
        $$("td.fw-bold").findBy(text(testCode)).closest("tr")
            .shouldHave(text("Inactive"));

        // Edit to make active again
        $$("td.fw-bold").findBy(text(testCode)).closest("tr")
            .$("button", 1).click();
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
        $("#code").setValue("Z"); // Use a different code to avoid conflicts
        $("#cancel-create-btn").click();

        $("table.table").shouldBe(visible);
        $$("td.fw-bold").findBy(text("Z")).shouldNot(exist);

        // Test cancel edit with existing data
        createTestGender();
        $$("td.fw-bold").findBy(text(testCode)).closest("tr")
            .$("button", 1).click();

        $("#code").clear();
        $("#code").setValue("Y");
        $("#cancel-edit-btn").click();

        // Verify original data unchanged
        $$("td.fw-bold").findBy(text(testCode)).shouldBe(visible);
        $$("td.fw-bold").findBy(text("Y")).shouldNot(exist);

        // Test cancel delete
        $$("td.fw-bold").findBy(text(testCode)).closest("tr")
            .$("button", 2).click();

        $(byText("Cancel")).click();

        // Verify record still exists
        $$("td.fw-bold").findBy(text(testCode)).shouldBe(visible);
    }

    private void createTestGender() {
        $("#create-new-btn").click();
        $("#code").setValue(testCode);
        $("#description").setValue(testDescription);
        $("#submit-create-btn").click();

        // Wait for the record to appear
        $$("td.fw-bold").findBy(text(testCode)).shouldBe(visible);
    }
}