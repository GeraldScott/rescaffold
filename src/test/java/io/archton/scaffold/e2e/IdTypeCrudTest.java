package io.archton.scaffold.e2e;

import io.archton.scaffold.e2e.base.BaseSelenideTest;
import io.archton.scaffold.e2e.pages.IdTypePage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.Duration;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.CollectionCondition.*;
import static com.codeborne.selenide.Selenide.$;

@DisplayName("IdType CRUD E2E Tests")
class IdTypeCrudTest extends BaseSelenideTest {
    
    private final IdTypePage idTypePage = new IdTypePage();
    
    @Test
    @DisplayName("Should display id type table when page loads")
    void shouldDisplayIdTypeTableWhenPageLoads() {
        idTypePage.openPage();
        
        idTypePage.getPageTitle().should(exist);
        idTypePage.getPageTitle().should(be(visible));
        idTypePage.getPageTitle().should(have(text("Types of identity documents")));
        
        idTypePage.getIdTypeTable().should(exist);
        idTypePage.getIdTypeTable().should(be(visible));
        
        idTypePage.getTableHeaders().shouldHave(texts("CODE", "DESCRIPTION", "CREATED BY", "CREATED AT", "UPDATED BY", "UPDATED AT", "ACTIONS"));
    }
    
    @Test
    @DisplayName("Should display Create button")
    void shouldDisplayCreateButton() {
        idTypePage.openPage();
        
        idTypePage.getCreateButton().should(exist);
        idTypePage.getCreateButton().should(be(visible));
    }
    
    @Test
    @DisplayName("Should display View, Edit, and Delete buttons for each table row")
    void shouldDisplayActionButtonsForEachTableRow() {
        idTypePage.openPage();
        
        idTypePage.getTableRows().shouldHave(sizeGreaterThan(0));
        
        int rowCount = idTypePage.getTableRows().size();
        for (int i = 0; i < rowCount; i++) {
            idTypePage.getViewButton(i).should(exist);
            idTypePage.getViewButton(i).should(be(visible));
            
            idTypePage.getEditButton(i).should(exist);
            idTypePage.getEditButton(i).should(be(visible));
            
            idTypePage.getDeleteButton(i).should(exist);
            idTypePage.getDeleteButton(i).should(be(visible));
        }
    }
    
    @Test
    @DisplayName("Should open Create screen when Create button is clicked")
    void shouldOpenCreateScreenWhenCreateButtonIsClicked() {
        idTypePage.openPage();
        
        idTypePage.getCreateButton().should(exist);
        idTypePage.getCreateButton().should(be(visible));
        
        idTypePage.clickCreate();
        
        idTypePage.getCodeInput().should(exist);
        idTypePage.getCodeInput().should(be(visible));
        
        idTypePage.getDescriptionInput().should(exist);
        idTypePage.getDescriptionInput().should(be(visible));
        
        idTypePage.getSaveButton().should(exist);
        idTypePage.getSaveButton().should(be(visible));
        
        idTypePage.getCancelButton().should(exist);
        idTypePage.getCancelButton().should(be(visible));
    }
    
    @Test
    @DisplayName("Should open View screen when View button is clicked")
    void shouldOpenViewScreenWhenViewButtonIsClicked() {
        idTypePage.openPage();
        
        idTypePage.getTableRows().shouldHave(sizeGreaterThan(0));
        
        idTypePage.getViewButton(0).should(exist);
        idTypePage.getViewButton(0).should(be(visible));
        idTypePage.getViewButton(0).click();
        
        idTypePage.getDetailCard().should(exist);
        idTypePage.getDetailCard().should(be(visible));
        
        idTypePage.getBackButton().should(exist);
        idTypePage.getBackButton().should(be(visible));
    }
    
    @Test
    @DisplayName("Should open Edit screen when Edit button is clicked")
    void shouldOpenEditScreenWhenEditButtonIsClicked() {
        idTypePage.openPage();
        
        idTypePage.getTableRows().shouldHave(sizeGreaterThan(0));
        
        idTypePage.getEditButton(0).should(exist);
        idTypePage.getEditButton(0).should(be(visible));
        idTypePage.getEditButton(0).click();
        
        idTypePage.getCodeInput().should(exist);
        idTypePage.getCodeInput().should(be(visible));
        
        idTypePage.getDescriptionInput().should(exist);
        idTypePage.getDescriptionInput().should(be(visible));
        
        idTypePage.getSaveButton().should(exist);
        idTypePage.getSaveButton().should(be(visible));
        
        idTypePage.getCancelButton().should(exist);
        idTypePage.getCancelButton().should(be(visible));
    }
    
    @Test
    @DisplayName("Should respond when Delete button is clicked")
    void shouldRespondWhenDeleteButtonIsClicked() {
        idTypePage.openPage();
        
        idTypePage.getTableRows().shouldHave(sizeGreaterThan(0));
        
        idTypePage.getDeleteButton(0).should(exist);
        idTypePage.getDeleteButton(0).should(be(visible));
        idTypePage.getDeleteButton(0).click();
        
        // After clicking delete, we should see the delete confirmation screen
        // Wait for HTMX updates to complete and verify the delete confirmation elements appear
        $("#confirm-delete-btn").should(appear, Duration.ofSeconds(3));
        $("#cancel-delete-btn").should(appear, Duration.ofSeconds(3));
        
        // Verify warning message appears
        $(".alert-warning").should(appear);
        
        // Check if we're still on a page with content
        // This verifies the delete button triggered the expected action (showing confirmation)
        $("body").should(exist);
    }
}