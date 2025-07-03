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
}