package io.archton.scaffold.e2e;

import io.archton.scaffold.e2e.base.BaseSelenideTest;
import io.archton.scaffold.e2e.pages.GenderPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.CollectionCondition.*;

@DisplayName("Gender CRUD E2E Tests")
class GenderCrudTest extends BaseSelenideTest {
    
    private final GenderPage genderPage = new GenderPage();
    
    @Test
    @DisplayName("Should display gender table when page loads")
    void shouldDisplayGenderTableWhenPageLoads() {
        // Open the gender page directly
        genderPage.openPage();
        
        // Verify page title
        genderPage.getPageTitle().should(exist);
        genderPage.getPageTitle().should(be(visible));
        genderPage.getPageTitle().should(have(text("Genders")));
        
        // Verify gender table exists and is visible
        genderPage.getGenderTable().should(exist);
        genderPage.getGenderTable().should(be(visible));
        
        // Verify table headers exist and contain expected text
        genderPage.getTableHeaders().shouldHave(texts("CODE", "DESCRIPTION", "CREATED BY", "CREATED AT", "UPDATED BY", "UPDATED AT", "ACTIONS"));
    }
    
    @Test
    @DisplayName("Should display Create button")
    void shouldDisplayCreateButton() {
        // Open the gender page
        genderPage.openPage();
        
        // Verify Create button exists and is visible
        genderPage.getCreateButton().should(exist);
        genderPage.getCreateButton().should(be(visible));
    }
    
    @Test
    @DisplayName("Should display View, Edit, and Delete buttons for each table row")
    void shouldDisplayActionButtonsForEachTableRow() {
        // Open the gender page
        genderPage.openPage();
        
        // Verify table has at least one row (assuming there's test data)
        genderPage.getTableRows().shouldHave(sizeGreaterThan(0));
        
        // For each row, verify View, Edit, and Delete buttons exist
        int rowCount = genderPage.getTableRows().size();
        for (int i = 0; i < rowCount; i++) {
            // Verify View button exists and is visible
            genderPage.getViewButton(i).should(exist);
            genderPage.getViewButton(i).should(be(visible));
            
            // Verify Edit button exists and is visible
            genderPage.getEditButton(i).should(exist);
            genderPage.getEditButton(i).should(be(visible));
            
            // Verify Delete button exists and is visible
            genderPage.getDeleteButton(i).should(exist);
            genderPage.getDeleteButton(i).should(be(visible));
        }
    }
}