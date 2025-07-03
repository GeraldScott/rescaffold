package io.archton.scaffold.e2e;

import io.archton.scaffold.e2e.base.BaseSelenideTest;
import io.archton.scaffold.e2e.pages.CountryPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.Duration;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.CollectionCondition.*;
import static com.codeborne.selenide.Selenide.$;

@DisplayName("Country CRUD E2E Tests")
class CountryCrudTest extends BaseSelenideTest {
    
    private final CountryPage countryPage = new CountryPage();
    
    @Test
    @DisplayName("Should display country table when page loads")
    void shouldDisplayCountryTableWhenPageLoads() {
        countryPage.openPage();
        
        countryPage.getPageTitle().should(exist);
        countryPage.getPageTitle().should(be(visible));
        countryPage.getPageTitle().should(have(text("Countries")));
        
        countryPage.getCountryTable().should(exist);
        countryPage.getCountryTable().should(be(visible));
        
        countryPage.getTableHeaders().shouldHave(texts("CODE", "NAME", "YEAR", "CCTLD", "CREATED BY", "CREATED AT", "UPDATED BY", "UPDATED AT", "ACTIONS"));
    }
    
    @Test
    @DisplayName("Should display Create button")
    void shouldDisplayCreateButton() {
        countryPage.openPage();
        
        countryPage.getCreateButton().should(exist);
        countryPage.getCreateButton().should(be(visible));
    }
    
    @Test
    @DisplayName("Should display View, Edit, and Delete buttons for each table row")
    void shouldDisplayActionButtonsForEachTableRow() {
        countryPage.openPage();
        
        countryPage.getTableRows().shouldHave(sizeGreaterThan(0));
        
        int rowCount = countryPage.getTableRows().size();
        for (int i = 0; i < rowCount; i++) {
            countryPage.getViewButton(i).should(exist);
            countryPage.getViewButton(i).should(be(visible));
            
            countryPage.getEditButton(i).should(exist);
            countryPage.getEditButton(i).should(be(visible));
            
            countryPage.getDeleteButton(i).should(exist);
            countryPage.getDeleteButton(i).should(be(visible));
        }
    }
    
    @Test
    @DisplayName("Should open Create screen when Create button is clicked")
    void shouldOpenCreateScreenWhenCreateButtonIsClicked() {
        countryPage.openPage();
        
        countryPage.getCreateButton().should(exist);
        countryPage.getCreateButton().should(be(visible));
        
        countryPage.clickCreate();
        
        countryPage.getCodeInput().should(exist);
        countryPage.getCodeInput().should(be(visible));
        
        countryPage.getNameInput().should(exist);
        countryPage.getNameInput().should(be(visible));
        
        countryPage.getSaveButton().should(exist);
        countryPage.getSaveButton().should(be(visible));
        
        countryPage.getCancelButton().should(exist);
        countryPage.getCancelButton().should(be(visible));
    }
    
    @Test
    @DisplayName("Should open View screen when View button is clicked")
    void shouldOpenViewScreenWhenViewButtonIsClicked() {
        countryPage.openPage();
        
        countryPage.getTableRows().shouldHave(sizeGreaterThan(0));
        
        countryPage.getViewButton(0).should(exist);
        countryPage.getViewButton(0).should(be(visible));
        countryPage.getViewButton(0).click();
        
        countryPage.getDetailCard().should(exist);
        countryPage.getDetailCard().should(be(visible));
        
        countryPage.getBackButton().should(exist);
        countryPage.getBackButton().should(be(visible));
    }
    
    @Test
    @DisplayName("Should open Edit screen when Edit button is clicked")
    void shouldOpenEditScreenWhenEditButtonIsClicked() {
        countryPage.openPage();
        
        countryPage.getTableRows().shouldHave(sizeGreaterThan(0));
        
        countryPage.getEditButton(0).should(exist);
        countryPage.getEditButton(0).should(be(visible));
        countryPage.getEditButton(0).click();
        
        countryPage.getCodeInput().should(exist);
        countryPage.getCodeInput().should(be(visible));
        
        countryPage.getNameInput().should(exist);
        countryPage.getNameInput().should(be(visible));
        
        countryPage.getSaveButton().should(exist);
        countryPage.getSaveButton().should(be(visible));
        
        countryPage.getCancelButton().should(exist);
        countryPage.getCancelButton().should(be(visible));
    }
    
    @Test
    @DisplayName("Should respond when Delete button is clicked")
    void shouldRespondWhenDeleteButtonIsClicked() {
        countryPage.openPage();
        
        countryPage.getTableRows().shouldHave(sizeGreaterThan(0));
        
        countryPage.getDeleteButton(0).should(exist);
        countryPage.getDeleteButton(0).should(be(visible));
        countryPage.getDeleteButton(0).click();
        
        countryPage.getCountryTable().should(appear, Duration.ofSeconds(3));
        
        $("body").should(exist);
    }
}