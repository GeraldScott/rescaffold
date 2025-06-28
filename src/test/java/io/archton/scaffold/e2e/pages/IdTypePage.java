package io.archton.scaffold.e2e.pages;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.*;

public class IdTypePage {

    // Page elements
    private final SelenideElement contentArea = $("#content-area");
    private final SelenideElement pageTitle = $("h1");
    private final SelenideElement idTypeTable = $("table.table");
    private final SelenideElement createButton = $("#create-new-btn");

    // Table headers
    private final ElementsCollection tableHeaders = $$("table.table thead th");

    // Table rows
    private final ElementsCollection tableRows = $$("table.table tbody tr");

    // Form elements (for create/edit)
    private final SelenideElement codeInput = $("#code");
    private final SelenideElement descriptionInput = $("#description");
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

    public IdTypePage openPage() {
        open("/id-types-ui");
        return this;
    }

    // Navigation methods
    public IdTypePage clickCreateButton() {
        createButton.scrollIntoView(true);
        sleep(500);
        createButton.click();
        return this;
    }

    public IdTypePage clickViewButton(Long idTypeId) {
        $("#view-btn-" + idTypeId).click();
        return this;
    }

    public IdTypePage clickEditButton(Long idTypeId) {
        $("#edit-btn-" + idTypeId).click();
        return this;
    }

    public IdTypePage clickDeleteButton(Long idTypeId) {
        $("#delete-btn-" + idTypeId).click();
        return this;
    }

    public IdTypePage clickBackButton() {
        backButton.click();
        return this;
    }

    public IdTypePage clickSubmitCreateButton() {
        submitCreateButton.click();
        return this;
    }

    public IdTypePage clickCancelCreateButton() {
        cancelCreateButton.click();
        return this;
    }

    public IdTypePage clickSubmitEditButton() {
        submitEditButton.click();
        return this;
    }

    public IdTypePage clickCancelEditButton() {
        cancelEditButton.click();
        return this;
    }

    public IdTypePage clickConfirmDeleteButton() {
        confirmDeleteButton.click();
        return this;
    }

    public IdTypePage clickCancelDeleteButton() {
        cancelDeleteButton.click();
        return this;
    }

    // Form input methods
    public IdTypePage enterCode(String code) {
        codeInput.setValue(code);
        return this;
    }

    public IdTypePage enterDescription(String description) {
        descriptionInput.setValue(description);
        return this;
    }

    // Getter methods for assertions
    public SelenideElement getContentArea() {
        return contentArea;
    }

    public SelenideElement getPageTitle() {
        return pageTitle;
    }

    public SelenideElement getIdTypeTable() {
        return idTypeTable;
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

    public SelenideElement getAlertMessage() {
        return alertMessage;
    }

    public SelenideElement getCodeValue() {
        return codeValue;
    }

    // Helper methods
    public boolean isTableVisible() {
        return idTypeTable.isDisplayed();
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

    public boolean hasIdTypeWithCode(String code) {
        return getRowByCode(code) != null;
    }
}