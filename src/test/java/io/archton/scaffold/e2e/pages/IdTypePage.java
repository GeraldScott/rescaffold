package io.archton.scaffold.e2e.pages;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.Selenide.open;

public class IdTypePage {
    
    private final SelenideElement pageTitle = $("h1");
    private final SelenideElement idTypeTable = $("table.table");
    private final ElementsCollection tableHeaders = $$("table.table th");
    private final SelenideElement createButton = $("#create-new-btn");
    
    private final ElementsCollection tableRows = $$("table.table tbody tr");
    private final ElementsCollection viewButtons = $$("button[id^='view-btn-']");
    private final ElementsCollection editButtons = $$("button[id^='edit-btn-']");
    private final ElementsCollection deleteButtons = $$("button[id^='delete-btn-']");
    
    private final SelenideElement codeInput = $("#code");
    private final SelenideElement descriptionInput = $("#description");
    private final SelenideElement saveButton = $("button[type='submit']");
    private final SelenideElement cancelButton = $("button.btn-outline-secondary[hx-get='/id-types-ui/table']");
    private final SelenideElement errorMessage = $(".alert-danger");
    
    private final SelenideElement detailCard = $(".card");
    private final SelenideElement backButton = $("button.btn-outline-secondary");
    
    public IdTypePage openPage() {
        open("/id-types-ui");
        return this;
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
    
    public SelenideElement getCreateButton() {
        return createButton;
    }
    
    public SelenideElement getCodeInput() {
        return codeInput;
    }
    
    public SelenideElement getDescriptionInput() {
        return descriptionInput;
    }
    
    public SelenideElement getSaveButton() {
        return saveButton;
    }
    
    public SelenideElement getCancelButton() {
        return cancelButton;
    }
    
    public IdTypePage clickCreate() {
        createButton.scrollTo().click();
        return this;
    }
    
    public IdTypePage fillIdTypeForm(String code, String description) {
        codeInput.setValue(code);
        descriptionInput.setValue(description);
        return this;
    }
    
    public IdTypePage clickSave() {
        saveButton.click();
        return this;
    }
    
    public IdTypePage clickCancel() {
        cancelButton.click();
        return this;
    }
    
    public SelenideElement getViewButton(int index) {
        return viewButtons.get(index);
    }
    
    public SelenideElement getEditButton(int index) {
        return editButtons.get(index);
    }
    
    public SelenideElement getDeleteButton(int index) {
        return deleteButtons.get(index);
    }
    
    public SelenideElement getErrorMessage() {
        return errorMessage;
    }
    
    public SelenideElement getDetailCard() {
        return detailCard;
    }
    
    public SelenideElement getBackButton() {
        return backButton;
    }
}