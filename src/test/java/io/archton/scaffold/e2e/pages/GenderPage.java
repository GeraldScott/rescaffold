package io.archton.scaffold.e2e.pages;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.Selenide.open;

public class GenderPage {
    
    // Page elements
    private final SelenideElement contentArea = $("#content-area");
    private final SelenideElement pageTitle = $("h1");
    private final SelenideElement genderTable = $("table.table");
    private final SelenideElement createButton = $("#create-new-btn");
    
    // Table headers
    private final ElementsCollection tableHeaders = $$("table.table thead th");
    
    // Table rows
    private final ElementsCollection tableRows = $$("table.table tbody tr");
    
    // Form elements (for create/edit)
    private final SelenideElement codeInput = $("#code");
    private final SelenideElement descriptionInput = $("#description");
    private final SelenideElement isActiveCheckbox = $("#isActive");
    private final SelenideElement submitCreateButton = $("#submit-create-btn");
    private final SelenideElement cancelCreateButton = $("#cancel-create-btn");
    private final SelenideElement submitEditButton = $("#submit-edit-btn");
    private final SelenideElement cancelEditButton = $("#cancel-edit-btn");
    private final SelenideElement confirmDeleteButton = $("#confirm-delete-btn");
    private final SelenideElement cancelDeleteButton = $("#cancel-delete-btn");
    
    // View page elements
    private final SelenideElement codeValue = $(".col-sm-8 .badge.bg-primary");
    private final SelenideElement backButton = $("button:has(.bi-arrow-left)");
    
    // Alert messages
    private final SelenideElement alertMessage = $(".alert");
    
    public GenderPage openPage() {
        open("/genders-ui");
        return this;
    }
    
    // Navigation methods
    public GenderPage clickCreateButton() {
        createButton.click();
        return this;
    }
    
    public GenderPage clickViewButton(Long genderId) {
        $("#view-btn-" + genderId).click();
        return this;
    }
    
    public GenderPage clickEditButton(Long genderId) {
        $("#edit-btn-" + genderId).click();
        return this;
    }
    
    public GenderPage clickDeleteButton(Long genderId) {
        $("#delete-btn-" + genderId).click();
        return this;
    }
    
    public GenderPage clickBackButton() {
        backButton.click();
        return this;
    }
    
    public GenderPage clickSubmitCreateButton() {
        submitCreateButton.click();
        return this;
    }
    
    public GenderPage clickCancelCreateButton() {
        cancelCreateButton.click();
        return this;
    }
    
    public GenderPage clickSubmitEditButton() {
        submitEditButton.click();
        return this;
    }
    
    public GenderPage clickCancelEditButton() {
        cancelEditButton.click();
        return this;
    }
    
    public GenderPage clickConfirmDeleteButton() {
        confirmDeleteButton.click();
        return this;
    }
    
    public GenderPage clickCancelDeleteButton() {
        cancelDeleteButton.click();
        return this;
    }
    
    // Form input methods
    public GenderPage enterCode(String code) {
        codeInput.setValue(code);
        return this;
    }
    
    public GenderPage enterDescription(String description) {
        descriptionInput.setValue(description);
        return this;
    }
    
    public GenderPage setActive(boolean active) {
        if (active != isActiveCheckbox.isSelected()) {
            isActiveCheckbox.click();
        }
        return this;
    }
    
    // Getter methods for assertions
    public SelenideElement getContentArea() {
        return contentArea;
    }
    
    public SelenideElement getPageTitle() {
        return pageTitle;
    }
    
    public SelenideElement getGenderTable() {
        return genderTable;
    }
    
    public ElementsCollection getTableHeaders() {
        return tableHeaders;
    }
    
    public ElementsCollection getTableRows() {
        return tableRows;
    }
    
    public SelenideElement getCodeInput() {
        return codeInput;
    }
    
    public SelenideElement getDescriptionInput() {
        return descriptionInput;
    }
    
    public SelenideElement getIsActiveCheckbox() {
        return isActiveCheckbox;
    }
    
    public SelenideElement getAlertMessage() {
        return alertMessage;
    }
    
    public SelenideElement getCodeValue() {
        return codeValue;
    }
    
    // Helper methods
    public boolean isTableVisible() {
        return genderTable.isDisplayed();
    }
    
    public int getRowCount() {
        return tableRows.size();
    }
    
    public SelenideElement getRowByCode(String code) {
        // Find the row containing a cell with fw-bold class and exact text
        for (SelenideElement row : tableRows) {
            SelenideElement codeCell = row.$("td.fw-bold");
            if (codeCell.exists() && codeCell.getText().equals(code)) {
                return row;
            }
        }
        return null;
    }
    
    public boolean hasGenderWithCode(String code) {
        return getRowByCode(code) != null;
    }
}