package io.archton.scaffold.e2e.pages;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.Selenide.open;

public class CountryPage {
    
    private final SelenideElement pageTitle = $("h1");
    private final SelenideElement countryTable = $("table.table");
    private final ElementsCollection tableHeaders = $$("table.table th");
    private final SelenideElement createButton = $("button[hx-get='/countries-ui/create']");
    
    private final ElementsCollection tableRows = $$("table.table tbody tr");
    private final ElementsCollection viewButtons = $$("button[id^='view-btn-']");
    private final ElementsCollection editButtons = $$("button[id^='edit-btn-']");
    private final ElementsCollection deleteButtons = $$("button[id^='delete-btn-']");
    
    private final SelenideElement codeInput = $("#code");
    private final SelenideElement nameInput = $("#name");
    private final SelenideElement yearInput = $("#year");
    private final SelenideElement cctldInput = $("#cctld");
    private final SelenideElement saveButton = $("button[type='submit']");
    private final SelenideElement cancelButton = $("button.btn-outline-secondary[hx-get='/countries-ui/table']");
    private final SelenideElement errorMessage = $(".alert-danger");
    
    private final SelenideElement detailCard = $(".card");
    private final SelenideElement backButton = $("button.btn-outline-secondary");
    
    public CountryPage openPage() {
        open("/countries-ui");
        return this;
    }
    
    public SelenideElement getPageTitle() {
        return pageTitle;
    }
    
    public SelenideElement getCountryTable() {
        return countryTable;
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
    
    public SelenideElement getNameInput() {
        return nameInput;
    }
    
    public SelenideElement getYearInput() {
        return yearInput;
    }
    
    public SelenideElement getCctldInput() {
        return cctldInput;
    }
    
    public SelenideElement getSaveButton() {
        return saveButton;
    }
    
    public SelenideElement getCancelButton() {
        return cancelButton;
    }
    
    public CountryPage clickCreate() {
        createButton.click();
        return this;
    }
    
    public CountryPage fillCountryForm(String code, String name) {
        codeInput.setValue(code);
        nameInput.setValue(name);
        return this;
    }
    
    public CountryPage fillCountryForm(String code, String name, String year, String cctld) {
        codeInput.setValue(code);
        nameInput.setValue(name);
        yearInput.setValue(year);
        cctldInput.setValue(cctld);
        return this;
    }
    
    public CountryPage clickSave() {
        saveButton.click();
        return this;
    }
    
    public CountryPage clickCancel() {
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