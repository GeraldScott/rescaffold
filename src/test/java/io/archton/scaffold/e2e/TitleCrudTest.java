package io.archton.scaffold.e2e;

import io.archton.scaffold.e2e.base.BaseSelenideTest;
import io.archton.scaffold.e2e.pages.TitlePage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.Duration;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.CollectionCondition.*;
import static com.codeborne.selenide.Selenide.$;

@DisplayName("Title CRUD E2E Tests")
class TitleCrudTest extends BaseSelenideTest {
    
    private final TitlePage titlePage = new TitlePage();
    
    @Test
    @DisplayName("Should display title table when page loads")
    void shouldDisplayTitleTableWhenPageLoads() {
        titlePage.openPage();
        
        titlePage.getPageTitle().should(exist);
        titlePage.getPageTitle().should(be(visible));
        titlePage.getPageTitle().should(have(text("Titles")));
        
        titlePage.getTitleTable().should(exist);
        titlePage.getTitleTable().should(be(visible));
        
        titlePage.getTableHeaders().shouldHave(texts("CODE", "DESCRIPTION", "CREATED BY", "CREATED AT", "UPDATED BY", "UPDATED AT", "ACTIONS"));
    }
    
    @Test
    @DisplayName("Should display Create button")
    void shouldDisplayCreateButton() {
        titlePage.openPage();
        
        titlePage.getCreateButton().should(exist);
        titlePage.getCreateButton().should(be(visible));
    }
    
    @Test
    @DisplayName("Should display View, Edit, and Delete buttons for each table row")
    void shouldDisplayActionButtonsForEachTableRow() {
        titlePage.openPage();
        
        titlePage.getTableRows().shouldHave(sizeGreaterThan(0));
        
        int rowCount = titlePage.getTableRows().size();
        for (int i = 0; i < rowCount; i++) {
            titlePage.getViewButton(i).should(exist);
            titlePage.getViewButton(i).should(be(visible));
            
            titlePage.getEditButton(i).should(exist);
            titlePage.getEditButton(i).should(be(visible));
            
            titlePage.getDeleteButton(i).should(exist);
            titlePage.getDeleteButton(i).should(be(visible));
        }
    }
    
    @Test
    @DisplayName("Should open Create screen when Create button is clicked")
    void shouldOpenCreateScreenWhenCreateButtonIsClicked() {
        titlePage.openPage();
        
        titlePage.getCreateButton().should(exist);
        titlePage.getCreateButton().should(be(visible));
        
        titlePage.clickCreate();
        
        titlePage.getCodeInput().should(exist);
        titlePage.getCodeInput().should(be(visible));
        
        titlePage.getDescriptionInput().should(exist);
        titlePage.getDescriptionInput().should(be(visible));
        
        titlePage.getSaveButton().should(exist);
        titlePage.getSaveButton().should(be(visible));
        
        titlePage.getCancelButton().should(exist);
        titlePage.getCancelButton().should(be(visible));
    }
    
    @Test
    @DisplayName("Should open View screen when View button is clicked")
    void shouldOpenViewScreenWhenViewButtonIsClicked() {
        titlePage.openPage();
        
        titlePage.getTableRows().shouldHave(sizeGreaterThan(0));
        
        titlePage.getViewButton(0).should(exist);
        titlePage.getViewButton(0).should(be(visible));
        titlePage.getViewButton(0).click();
        
        titlePage.getDetailCard().should(exist);
        titlePage.getDetailCard().should(be(visible));
        
        titlePage.getBackButton().should(exist);
        titlePage.getBackButton().should(be(visible));
    }
    
    @Test
    @DisplayName("Should open Edit screen when Edit button is clicked")
    void shouldOpenEditScreenWhenEditButtonIsClicked() {
        titlePage.openPage();
        
        titlePage.getTableRows().shouldHave(sizeGreaterThan(0));
        
        titlePage.getEditButton(0).should(exist);
        titlePage.getEditButton(0).should(be(visible));
        titlePage.getEditButton(0).click();
        
        titlePage.getCodeInput().should(exist);
        titlePage.getCodeInput().should(be(visible));
        
        titlePage.getDescriptionInput().should(exist);
        titlePage.getDescriptionInput().should(be(visible));
        
        titlePage.getSaveButton().should(exist);
        titlePage.getSaveButton().should(be(visible));
        
        titlePage.getCancelButton().should(exist);
        titlePage.getCancelButton().should(be(visible));
    }
    
    @Test
    @DisplayName("Should respond when Delete button is clicked")
    void shouldRespondWhenDeleteButtonIsClicked() {
        titlePage.openPage();
        
        titlePage.getTableRows().shouldHave(sizeGreaterThan(0));
        
        titlePage.getDeleteButton(0).should(exist);
        titlePage.getDeleteButton(0).should(be(visible));
        titlePage.getDeleteButton(0).click();
        
        titlePage.getTitleTable().should(appear, Duration.ofSeconds(3));
        
        $("body").should(exist);
    }
}