package io.archton.scaffold.e2e.pages;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.Selenide.open;

public class TitlePage {
    
    private final SelenideElement pageTitle = $("h1");
    private final SelenideElement titleTable = $("table.table");
    private final ElementsCollection tableHeaders = $$("table.table th");
    private final SelenideElement createButton = $("button[hx-get='/titles-ui/create']");
    
    private final ElementsCollection tableRows = $$("table.table tbody tr");
    private final ElementsCollection viewButtons = $$("button[id^='view-btn-']");
    private final ElementsCollection editButtons = $$("button[id^='edit-btn-']");
    private final ElementsCollection deleteButtons = $$("button[id^='delete-btn-']");
    
    private final SelenideElement codeInput = $("#code");
    private final SelenideElement descriptionInput = $("#description");
    private final SelenideElement saveButton = $("button[type='submit']");
    private final SelenideElement cancelButton = $("button.btn-outline-secondary[hx-get='/titles-ui/table']");
    private final SelenideElement errorMessage = $(".alert-danger");
    
    private final SelenideElement detailCard = $(".card");
    private final SelenideElement backButton = $("button.btn-outline-secondary");
    
    public TitlePage openPage() {
        open("/titles-ui");
        return this;
    }
    
    public SelenideElement getPageTitle() {
        return pageTitle;
    }
    
    public SelenideElement getTitleTable() {
        return titleTable;
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
    
    public TitlePage clickCreate() {
        createButton.click();
        return this;
    }
    
    public TitlePage fillTitleForm(String code, String description) {
        codeInput.setValue(code);
        descriptionInput.setValue(description);
        return this;
    }
    
    public TitlePage clickSave() {
        saveButton.click();
        return this;
    }
    
    public TitlePage clickCancel() {
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