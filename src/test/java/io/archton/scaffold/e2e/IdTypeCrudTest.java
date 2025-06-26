package io.archton.scaffold.e2e;

import com.codeborne.selenide.SelenideElement;
import io.archton.scaffold.e2e.base.BaseSelenideTest;
import io.archton.scaffold.e2e.pages.IdTypePage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.codeborne.selenide.CollectionCondition.size;
import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("IdType CRUD E2E Tests")
class IdTypeCrudTest extends BaseSelenideTest {

    private final IdTypePage idTypePage = new IdTypePage();

    @BeforeEach
    void setUp() {
        // Navigate to id-types page before each test
        idTypePage.openPage();

        // Wait for table to load
        idTypePage.getIdTypeTable().should(exist);
    }

    @Test
    @DisplayName("Should display id type list table with correct headers")
    void shouldDisplayIdTypeListTable() {
        // Verify table exists and is visible
        idTypePage.getIdTypeTable().should(exist);
        idTypePage.getIdTypeTable().should(be(visible));
        assertTrue(idTypePage.isTableVisible(), "IdType table should be visible");

        // Verify table headers
        var headers = idTypePage.getTableHeaders();
        headers.shouldHave(size(8));
        headers.get(0).should(have(text("Code")));
        headers.get(1).should(have(text("Description")));
        headers.get(2).should(have(text("Created By")));
        headers.get(3).should(have(text("Created At")));
        headers.get(4).should(have(text("Updated By")));
        headers.get(5).should(have(text("Updated At")));
        headers.get(6).should(have(text("Active")));
        headers.get(7).should(have(text("Actions")));

        // Verify Create button exists
        $("#create-new-btn").should(exist);
        $("#create-new-btn").should(be(visible));
        $("#create-new-btn").should(have(text("Create")));
    }

    @Test
    @DisplayName("Should create a new id type successfully")
    void shouldCreateNewIdType() {
        // Generate unique code and description for the test
        String uniqueCode = findUniqueCode();
        String description = "Test IdType " + System.currentTimeMillis();

        // Create a new id type using the extracted method
        createIdType(uniqueCode, description, true);

        // Verify the new id type appears in the table
        assertTrue(idTypePage.hasIdTypeWithCode(uniqueCode),
                "Newly created id type with code " + uniqueCode + " should exist");

        // Get the row and verify the description and active status
        SelenideElement newRow = idTypePage.getRowByCode(uniqueCode);
        assertNotNull(newRow, "New id type row should not be null");

        // Verify description
        newRow.$$("td").get(1).shouldHave(text(description));

        // Verify active status
        newRow.$$("td").get(6).$(".badge").shouldHave(text("Active"));

        // Verify creation details
        newRow.$$("td").get(2).shouldHave(text("system")); // Created by
        newRow.$$("td").get(3).shouldNotBe(empty); // Created at
    }

    @Test
    @DisplayName("Should view id type details")
    void shouldViewIdTypeDetails() {
        // Find first row and get its ID
        var firstRow = idTypePage.getTableRows().first();
        firstRow.should(exist);

        // Extract id type ID from the view button
        var viewButton = firstRow.$("button[id^='view-btn-']");
        var idTypeId = viewButton.getAttribute("id").replace("view-btn-", "");

        // Click View button
        idTypePage.clickViewButton(Long.parseLong(idTypeId));

        // Verify view form loads
        idTypePage.getContentArea().should(exist);
        idTypePage.getCodeValue().should(exist);
        idTypePage.getCodeValue().should(be(visible));

        // Verify Back button exists
        $("button:has(.bi-arrow-left)").should(exist);
        $("button:has(.bi-arrow-left)").should(have(text("Back")));

        // Click Back button
        idTypePage.clickBackButton();

        // Verify return to list
        idTypePage.getIdTypeTable().should(exist);
        idTypePage.getPageTitle().should(have(text("IdTypes")));
    }

    @Test
    @DisplayName("Should edit an existing id type")
    void shouldEditExistingIdType() {
        // Find first row and get its ID
        var firstRow = idTypePage.getTableRows().first();
        firstRow.should(exist);

        // Extract id type ID and current code from the row
        var editButton = firstRow.$("button[id^='edit-btn-']");
        var idTypeId = Long.parseLong(editButton.getAttribute("id").replace("edit-btn-", ""));
        var currentCode = firstRow.$("td.fw-bold").getText();
        var currentDescription = firstRow.$$("td").get(1).getText();

        // Generate a unique code and description
        String newCode = findUniqueCode();
        String newDescription = "Updated " + System.currentTimeMillis();

        // Click Edit button
        idTypePage.clickEditButton(idTypeId);

        // Verify edit form loads
        idTypePage.getCodeInput().should(exist);
        idTypePage.getDescriptionInput().should(exist);

        // Make changes
        idTypePage.enterCode(newCode)
                .enterDescription(newDescription)
                .setActive(false);

        // Submit changes
        idTypePage.clickSubmitEditButton();

        // Verify return to list
        idTypePage.getIdTypeTable().should(exist);

        // Find the updated row by code and verify
        assertTrue(idTypePage.hasIdTypeWithCode(newCode), "Updated id type with code " + newCode + " should exist");

        SelenideElement updatedRow = idTypePage.getRowByCode(newCode);
        assertNotNull(updatedRow, "Updated row should not be null");

        // Verify description was updated
        updatedRow.$$("td").get(1).shouldHave(text(newDescription));

        // Verify active status
        updatedRow.$$("td").get(6).$(".badge").shouldHave(text("Inactive"));
    }

    @Test
    @DisplayName("Should delete an existing id type")
    void shouldDeleteExistingIdType() {
        // Create a new id type to ensure we have something to delete
        String uniqueCode = findUniqueCode();
        String description = "Delete Test " + System.currentTimeMillis();

        // Create the id type using the extracted method
        createIdType(uniqueCode, description, true);

        // Verify the new id type appears in the table
        assertTrue(idTypePage.hasIdTypeWithCode(uniqueCode),
                "Newly created id type with code " + uniqueCode + " should exist before deletion");

        // Get the row and extract the ID for deletion
        SelenideElement targetRow = idTypePage.getRowByCode(uniqueCode);
        assertNotNull(targetRow, "Target row should not be null");

        // Extract id type ID from the delete button
        SelenideElement deleteButton = targetRow.$("button[id^='delete-btn-']");
        Long idTypeId = Long.parseLong(deleteButton.getAttribute("id").replace("delete-btn-", ""));

        // Click Delete button
        idTypePage.clickDeleteButton(idTypeId);

        // Verify delete confirmation page loads
        idTypePage.getContentArea().should(exist);
        $(".card-header.bg-danger").should(exist);
        $(".card-header.bg-danger").should(have(text("Delete IdType")));
        $(".alert.alert-warning").should(exist);
        $("#confirm-delete-btn").should(exist);
        $("#cancel-delete-btn").should(exist);

        // Verify the correct code and description are shown
        $(".badge.bg-primary.fs-6").should(have(text(uniqueCode)));

        // Click Confirm Delete button
        idTypePage.clickConfirmDeleteButton();

        // Verify return to list
        idTypePage.getIdTypeTable().should(exist);

        // Verify the id type is no longer in the table
        assertFalse(idTypePage.hasIdTypeWithCode(uniqueCode),
                "IdType with code " + uniqueCode + " should not exist after deletion");
    }

    /**
     * Helper method to create a new id type with the given attributes
     *
     * @param code The id type code (uppercase characters)
     * @param description The id type description
     * @param isActive Whether the id type should be active
     */
    private void createIdType(String code, String description, boolean isActive) {
        // Click Create button
        idTypePage.clickCreateButton();

        // Verify create form loads
        idTypePage.getCodeInput().should(exist);
        idTypePage.getDescriptionInput().should(exist);

        // Fill in the form
        idTypePage.enterCode(code)
                .enterDescription(description);

        // Set active status if needed (active by default)
        if (!isActive) {
            idTypePage.setActive(false);
        }

        // Submit the form
        idTypePage.clickSubmitCreateButton();

        // Verify return to list
        idTypePage.getIdTypeTable().should(exist);
    }

    /**
     * Helper method to find a unique code not in the table
     *
     * @return A unique code
     */
    private String findUniqueCode() {
        // Generate a code from available uppercase letters
        String availableChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

        // Try each letter combination until we find one that's not in use
        for (int i = 0; i < availableChars.length(); i++) {
            String candidateCode = String.valueOf(availableChars.charAt(i));
            if (!idTypePage.hasIdTypeWithCode(candidateCode)) {
                return candidateCode;
            }
        }

        // Try two-character combinations
        for (int i = 0; i < availableChars.length(); i++) {
            for (int j = 0; j < availableChars.length(); j++) {
                String candidateCode = String.valueOf(availableChars.charAt(i)) + 
                                       String.valueOf(availableChars.charAt(j));
                if (!idTypePage.hasIdTypeWithCode(candidateCode)) {
                    return candidateCode;
                }
            }
        }

        // If all combinations are in use (unlikely), use timestamp-based code
        return "IT" + String.valueOf(System.currentTimeMillis()).substring(8);
    }
}