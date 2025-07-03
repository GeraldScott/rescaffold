package io.archton.scaffold.e2e;

import io.archton.scaffold.e2e.base.BaseSelenideTest;
import io.archton.scaffold.e2e.pages.PersonPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.Duration;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.CollectionCondition.*;
import static com.codeborne.selenide.Selenide.$;

@DisplayName("Person CRUD E2E Tests")
class PersonCrudTest extends BaseSelenideTest {
    
    private final PersonPage personPage = new PersonPage();
    
    @Test
    @DisplayName("Should display person table when page loads")
    void shouldDisplayPersonTableWhenPageLoads() {
        personPage.openPage();
        
        personPage.getPageTitle().should(exist);
        personPage.getPageTitle().should(be(visible));
        personPage.getPageTitle().should(have(text("People")));
        
        personPage.getPersonTable().should(exist);
        personPage.getPersonTable().should(be(visible));
        
        personPage.getTableHeaders().shouldHave(texts("FIRST NAME", "LAST NAME", "EMAIL", "ID NUMBER", "ID TYPE", "GENDER", "TITLE", "COUNTRY", "CREATED BY", "CREATED AT", "UPDATED BY", "UPDATED AT", "ACTIONS"));
    }
    
    @Test
    @DisplayName("Should display Create button")
    void shouldDisplayCreateButton() {
        personPage.openPage();
        
        personPage.getCreateButton().should(exist);
        personPage.getCreateButton().should(be(visible));
    }
    
    @Test
    @DisplayName("Should display View, Edit, and Delete buttons for each table row")
    void shouldDisplayActionButtonsForEachTableRow() {
        personPage.openPage();
        
        personPage.getTableRows().shouldHave(sizeGreaterThan(0));
        
        int rowCount = personPage.getTableRows().size();
        for (int i = 0; i < rowCount; i++) {
            personPage.getViewButton(i).should(exist);
            personPage.getViewButton(i).should(be(visible));
            
            personPage.getEditButton(i).should(exist);
            personPage.getEditButton(i).should(be(visible));
            
            personPage.getDeleteButton(i).should(exist);
            personPage.getDeleteButton(i).should(be(visible));
        }
    }
    
    @Test
    @DisplayName("Should open Create screen when Create button is clicked")
    void shouldOpenCreateScreenWhenCreateButtonIsClicked() {
        personPage.openPage();
        
        personPage.getCreateButton().should(exist);
        personPage.getCreateButton().should(be(visible));
        
        personPage.clickCreate();
        
        personPage.getFirstNameInput().should(exist);
        personPage.getFirstNameInput().should(be(visible));
        
        personPage.getLastNameInput().should(exist);
        personPage.getLastNameInput().should(be(visible));
        
        personPage.getEmailInput().should(exist);
        personPage.getEmailInput().should(be(visible));
        
        personPage.getSaveButton().should(exist);
        personPage.getSaveButton().should(be(visible));
        
        personPage.getCancelButton().should(exist);
        personPage.getCancelButton().should(be(visible));
    }
    
    @Test
    @DisplayName("Should open View screen when View button is clicked")
    void shouldOpenViewScreenWhenViewButtonIsClicked() {
        personPage.openPage();
        
        personPage.getTableRows().shouldHave(sizeGreaterThan(0));
        
        personPage.getViewButton(0).should(exist);
        personPage.getViewButton(0).should(be(visible));
        personPage.getViewButton(0).click();
        
        personPage.getDetailCard().should(exist);
        personPage.getDetailCard().should(be(visible));
        
        personPage.getBackButton().should(exist);
        personPage.getBackButton().should(be(visible));
    }
    
    @Test
    @DisplayName("Should open Edit screen when Edit button is clicked")
    void shouldOpenEditScreenWhenEditButtonIsClicked() {
        personPage.openPage();
        
        personPage.getTableRows().shouldHave(sizeGreaterThan(0));
        
        personPage.getEditButton(0).should(exist);
        personPage.getEditButton(0).should(be(visible));
        personPage.getEditButton(0).click();
        
        personPage.getFirstNameInput().should(exist);
        personPage.getFirstNameInput().should(be(visible));
        
        personPage.getLastNameInput().should(exist);
        personPage.getLastNameInput().should(be(visible));
        
        personPage.getEmailInput().should(exist);
        personPage.getEmailInput().should(be(visible));
        
        personPage.getSaveButton().should(exist);
        personPage.getSaveButton().should(be(visible));
        
        personPage.getCancelButton().should(exist);
        personPage.getCancelButton().should(be(visible));
    }
    
    @Test
    @DisplayName("Should respond when Delete button is clicked")
    void shouldRespondWhenDeleteButtonIsClicked() {
        personPage.openPage();
        
        personPage.getTableRows().shouldHave(sizeGreaterThan(0));
        
        personPage.getDeleteButton(0).should(exist);
        personPage.getDeleteButton(0).should(be(visible));
        personPage.getDeleteButton(0).click();
        
        personPage.getPersonTable().should(appear, Duration.ofSeconds(3));
        
        $("body").should(exist);
    }
}