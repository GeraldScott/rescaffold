package io.archton.scaffold.e2e;

import com.codeborne.selenide.SelenideElement;
import io.archton.scaffold.e2e.base.BaseSelenideTest;
import io.archton.scaffold.e2e.pages.GenderPage;
import io.archton.scaffold.e2e.pages.HomePage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.codeborne.selenide.CollectionCondition.size;
import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.*;
import static com.codeborne.selenide.WebDriverConditions.url;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Gender CRUD E2E Tests")
class GenderCrudTest extends BaseSelenideTest {

    private final HomePage homePage = new HomePage();
    private final GenderPage genderPage = new GenderPage();

    @BeforeEach
    void setUp() {
        // Navigate to genders page before each test
        genderPage.openPage();

        // Wait for table to load
        genderPage.getGenderTable().should(exist);
    }

    @Test
    @DisplayName("Should navigate to Genders page from Maintenance menu")
    void shouldNavigateToGendersPage() {
        // Navigate to home page
        homePage.openPage();

        // Click Maintenance dropdown
        homePage.clickMaintenanceDropdown();

        // Verify Genders link is visible
        homePage.getGendersDropdownLink().should(exist);
        homePage.getGendersDropdownLink().should(be(visible));
        homePage.getGendersDropdownLink().should(have(text("Genders")));

        // Click Genders link
        homePage.clickGendersLink();

        // Verify navigation to Genders page
        webdriver().shouldHave(url(BASE_URL + "/genders-ui"));

        // Verify page loads correctly
        genderPage.getPageTitle().should(have(text("Genders")));
        genderPage.getContentArea().should(exist);
        genderPage.getGenderTable().should(exist);
    }

    @Test
    @DisplayName("Should display gender list table with correct headers")
    void shouldDisplayGenderListTable() {
        // Verify table exists and is visible
        genderPage.getGenderTable().should(exist);
        genderPage.getGenderTable().should(be(visible));
        assertTrue(genderPage.isTableVisible(), "Gender table should be visible");

        // Verify table headers
        var headers = genderPage.getTableHeaders();
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
    @DisplayName("Should create a new gender successfully")
    void shouldCreateNewGender() {
        // Generate unique code and description for the test
        String uniqueCode = findUniqueCode();
        String description = "Test Gender " + System.currentTimeMillis();

        // Create a new gender using the extracted method
        createGender(uniqueCode, description, true);

        // Verify the new gender appears in the table
        assertTrue(genderPage.hasGenderWithCode(uniqueCode),
                "Newly created gender with code " + uniqueCode + " should exist");

        // Get the row and verify the description and active status
        SelenideElement newRow = genderPage.getRowByCode(uniqueCode);
        assertNotNull(newRow, "New gender row should not be null");

        // Verify description
        newRow.$$("td").get(1).shouldHave(text(description));

        // Verify active status
        newRow.$$("td").get(6).$(".badge").shouldHave(text("Active"));

        // Verify creation details
        newRow.$$("td").get(2).shouldHave(text("system")); // Created by
        newRow.$$("td").get(3).shouldNotBe(empty); // Created at
    }

    @Test
    @DisplayName("Should view gender details")
    void shouldViewGenderDetails() {
        // Find first row and get its ID
        var firstRow = genderPage.getTableRows().first();
        firstRow.should(exist);

        // Extract gender ID from the view button
        var viewButton = firstRow.$("button[id^='view-btn-']");
        var genderId = viewButton.getAttribute("id").replace("view-btn-", "");

        // Click View button
        genderPage.clickViewButton(Long.parseLong(genderId));

        // Verify view form loads
        genderPage.getContentArea().should(exist);
        genderPage.getCodeValue().should(exist);
        genderPage.getCodeValue().should(be(visible));

        // Verify Back button exists
        $("button:has(.bi-arrow-left)").should(exist);
        $("button:has(.bi-arrow-left)").should(have(text("Back")));

        // Click Back button
        genderPage.clickBackButton();

        // Verify return to list
        genderPage.getGenderTable().should(exist);
        genderPage.getPageTitle().should(have(text("Genders")));
    }

    @Test
    @DisplayName("Should edit an existing gender")
    void shouldEditExistingGender() {
        // Find first row and get its ID
        var firstRow = genderPage.getTableRows().first();
        firstRow.should(exist);

        // Extract gender ID and current code from the row
        var editButton = firstRow.$("button[id^='edit-btn-']");
        var genderId = Long.parseLong(editButton.getAttribute("id").replace("edit-btn-", ""));
        var currentCode = firstRow.$("td.fw-bold").getText();
        var currentDescription = firstRow.$$("td").get(1).getText();

        // Generate a unique code and description
        String newCode = findUniqueCode();
        String newDescription = "Updated " + System.currentTimeMillis();

        // Click Edit button
        genderPage.clickEditButton(genderId);

        // Verify edit form loads
        genderPage.getCodeInput().should(exist);
        genderPage.getDescriptionInput().should(exist);

        // Make changes
        genderPage.enterCode(newCode)
                .enterDescription(newDescription)
                .setActive(false);

        // Submit changes
        genderPage.clickSubmitEditButton();

        // Verify return to list
        genderPage.getGenderTable().should(exist);

        // Find the updated row by code and verify
        assertTrue(genderPage.hasGenderWithCode(newCode), "Updated gender with code " + newCode + " should exist");

        SelenideElement updatedRow = genderPage.getRowByCode(newCode);
        assertNotNull(updatedRow, "Updated row should not be null");

        // Verify description was updated
        updatedRow.$$("td").get(1).shouldHave(text(newDescription));

        // Verify active status
        updatedRow.$$("td").get(6).$(".badge").shouldHave(text("Inactive"));
    }

    @Test
    @DisplayName("Should delete an existing gender")
    void shouldDeleteExistingGender() {
        // Create a new gender to ensure we have something to delete
        String uniqueCode = findUniqueCode();
        String description = "Delete Test " + System.currentTimeMillis();

        // Create the gender using the extracted method
        createGender(uniqueCode, description, true);

        // Verify the new gender appears in the table
        assertTrue(genderPage.hasGenderWithCode(uniqueCode),
                "Newly created gender with code " + uniqueCode + " should exist before deletion");

        // Get the row and extract the ID for deletion
        SelenideElement targetRow = genderPage.getRowByCode(uniqueCode);
        assertNotNull(targetRow, "Target row should not be null");

        // Extract gender ID from the delete button
        SelenideElement deleteButton = targetRow.$("button[id^='delete-btn-']");
        Long genderId = Long.parseLong(deleteButton.getAttribute("id").replace("delete-btn-", ""));

        // Click Delete button
        genderPage.clickDeleteButton(genderId);

        // Verify delete confirmation page loads
        genderPage.getContentArea().should(exist);
        $(".card-header.bg-danger").should(exist);
        $(".card-header.bg-danger").should(have(text("Delete Gender")));
        $(".alert.alert-warning").should(exist);
        $("#confirm-delete-btn").should(exist);
        $("#cancel-delete-btn").should(exist);

        // Verify the correct code and description are shown
        $(".badge.bg-primary.fs-6").should(have(text(uniqueCode)));

        // Click Confirm Delete button
        genderPage.clickConfirmDeleteButton();

        // Verify return to list
        genderPage.getGenderTable().should(exist);

        // Verify the gender is no longer in the table
        assertFalse(genderPage.hasGenderWithCode(uniqueCode),
                "Gender with code " + uniqueCode + " should not exist after deletion");
    }

    /**
     * Helper method to create a new gender with the given attributes
     *
     * @param code The gender code (single uppercase character)
     * @param description The gender description
     * @param isActive Whether the gender should be active
     */
    private void createGender(String code, String description, boolean isActive) {
        // Click Create button
        genderPage.clickCreateButton();

        // Verify create form loads
        genderPage.getCodeInput().should(exist);
        genderPage.getDescriptionInput().should(exist);

        // Fill in the form
        genderPage.enterCode(code)
                .enterDescription(description);

        // Set active status if needed (active by default)
        if (!isActive) {
            genderPage.setActive(false);
        }

        // Submit the form
        genderPage.clickSubmitCreateButton();

        // Verify return to list
        genderPage.getGenderTable().should(exist);
    }

    /**
     * Helper method to find a unique single-character code not in the table
     *
     * @return A unique single-character code
     */
    private String findUniqueCode() {
        // Generate a code from available uppercase letters
        String availableChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

        // Try each letter until we find one that's not in use
        for (int i = 0; i < availableChars.length(); i++) {
            String candidateCode = String.valueOf(availableChars.charAt(i));
            if (!genderPage.hasGenderWithCode(candidateCode)) {
                return candidateCode;
            }
        }

        // If all codes are in use (unlikely), generate a timestamp-based unique code
        // This should never happen with only 26 possibilities and likely fewer records
        return "Z";
    }
}