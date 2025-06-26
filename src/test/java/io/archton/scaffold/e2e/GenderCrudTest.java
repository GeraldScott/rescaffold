package io.archton.scaffold.e2e;

import io.archton.scaffold.e2e.base.BaseSelenideTest;
import io.archton.scaffold.e2e.pages.GenderPage;
import io.archton.scaffold.e2e.pages.HomePage;
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
        // Navigate directly to genders page
        genderPage.openPage();
        
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
    @DisplayName("Should view gender details")
    void shouldViewGenderDetails() {
        // Navigate to genders page
        genderPage.openPage();
        
        // Wait for table to load
        genderPage.getGenderTable().should(exist);
        
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

}