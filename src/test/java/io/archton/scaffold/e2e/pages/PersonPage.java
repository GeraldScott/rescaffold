package io.archton.scaffold.e2e.pages;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.Selenide.open;

public class PersonPage {
    
    private final SelenideElement pageTitle = $("h1");
    private final SelenideElement personTable = $("table.table");
    private final ElementsCollection tableHeaders = $$("table.table th");
    private final SelenideElement createButton = $("#create-new-btn");
    
    private final ElementsCollection tableRows = $$("table.table tbody tr");
    private final ElementsCollection viewButtons = $$("button[id^='view-btn-']");
    private final ElementsCollection editButtons = $$("button[id^='edit-btn-']");
    private final ElementsCollection deleteButtons = $$("button[id^='delete-btn-']");
    
    private final SelenideElement firstNameInput = $("#firstName");
    private final SelenideElement lastNameInput = $("#lastName");
    private final SelenideElement emailInput = $("#email");
    private final SelenideElement genderSelect = $("#genderId");
    private final SelenideElement titleSelect = $("#titleId");
    private final SelenideElement saveButton = $("button[type='submit']");
    private final SelenideElement cancelButton = $("button.btn-outline-secondary[hx-get='/persons-ui/table']");
    private final SelenideElement errorMessage = $(".alert-danger");
    
    private final SelenideElement detailCard = $(".card");
    private final SelenideElement backButton = $("button.btn-outline-secondary");
    
    public PersonPage openPage() {
        open("/persons-ui");
        return this;
    }
    
    public SelenideElement getPageTitle() {
        return pageTitle;
    }
    
    public SelenideElement getPersonTable() {
        return personTable;
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
    
    public SelenideElement getFirstNameInput() {
        return firstNameInput;
    }
    
    public SelenideElement getLastNameInput() {
        return lastNameInput;
    }
    
    public SelenideElement getEmailInput() {
        return emailInput;
    }
    
    
    public SelenideElement getGenderSelect() {
        return genderSelect;
    }
    
    public SelenideElement getTitleSelect() {
        return titleSelect;
    }
    
    
    public SelenideElement getSaveButton() {
        return saveButton;
    }
    
    public SelenideElement getCancelButton() {
        return cancelButton;
    }
    
    public PersonPage clickCreate() {
        createButton.scrollTo().click();
        return this;
    }
    
    public PersonPage fillPersonForm(String firstName, String lastName, String email) {
        firstNameInput.setValue(firstName);
        lastNameInput.setValue(lastName);
        emailInput.setValue(email);
        return this;
    }
    
    public PersonPage clickSave() {
        saveButton.click();
        return this;
    }
    
    public PersonPage clickCancel() {
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