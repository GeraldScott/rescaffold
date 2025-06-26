package io.archton.scaffold.e2e.pages;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.*;

public class PersonPage {

    // Page elements
    private final SelenideElement contentArea = $("#content-area");
    private final SelenideElement pageTitle = $("h1");
    private final SelenideElement personTable = $("table.table");
    private final SelenideElement createButton = $("#create-new-btn");

    // Table headers
    private final ElementsCollection tableHeaders = $$("table.table thead th");

    // Table rows
    private final ElementsCollection tableRows = $$("table.table tbody tr");

    // Form elements (for create/edit)
    private final SelenideElement firstNameInput = $("#firstName");
    private final SelenideElement lastNameInput = $("#lastName");
    private final SelenideElement emailInput = $("#email");
    private final SelenideElement idNumberInput = $("#idNumber");
    private final SelenideElement titleSelect = $("#titleId");
    private final SelenideElement genderSelect = $("#genderId");
    private final SelenideElement idTypeSelect = $("#idTypeId");
    private final SelenideElement isActiveCheckbox = $("#isActive");
    private final SelenideElement submitCreateButton = $("#submit-create-btn");
    private final SelenideElement cancelCreateButton = $("#cancel-create-btn");
    private final SelenideElement submitEditButton = $("#submit-edit-btn");
    private final SelenideElement cancelEditButton = $("#cancel-edit-btn");
    private final SelenideElement saveButton = $("button[type='submit']");

    // View page elements
    private final SelenideElement firstNameValue = $("#firstName-value");
    private final SelenideElement lastNameValue = $("#lastName-value");
    private final SelenideElement emailValue = $("#email-value");
    private final SelenideElement backButton = $("button:has(.bi-arrow-left)");

    // Delete modal elements
    private final SelenideElement deleteModal = $("#deleteModal");
    private final SelenideElement confirmDeleteButton = $("#confirm-delete-btn");
    private final SelenideElement cancelDeleteButton = $("#cancel-delete-btn");

    // Alert messages
    private final SelenideElement alertMessage = $(".alert");
    private final ElementsCollection validationErrors = $$(".invalid-feedback");

    public PersonPage openPage() {
        open("/persons-ui");
        return this;
    }

    // Navigation methods
    public PersonPage clickCreateButton() {
        createButton.scrollIntoView(true);
        sleep(500);
        createButton.click();
        return this;
    }

    public PersonPage clickViewButton(Long personId) {
        $("#view-btn-" + personId).click();
        return this;
    }

    public PersonPage clickEditButton(Long personId) {
        $("#edit-btn-" + personId).click();
        return this;
    }

    public PersonPage clickDeleteButton(Long personId) {
        $("#delete-btn-" + personId).click();
        return this;
    }

    public PersonPage clickBackButton() {
        backButton.click();
        return this;
    }

    public PersonPage clickSaveButton() {
        saveButton.click();
        return this;
    }

    public PersonPage clickSubmitCreateButton() {
        submitCreateButton.click();
        return this;
    }

    public PersonPage clickCancelCreateButton() {
        cancelCreateButton.click();
        return this;
    }

    public PersonPage clickSubmitEditButton() {
        submitEditButton.click();
        return this;
    }

    public PersonPage clickCancelEditButton() {
        cancelEditButton.click();
        return this;
    }

    public PersonPage clickConfirmDeleteButton() {
        confirmDeleteButton.click();
        return this;
    }

    public PersonPage clickCancelDeleteButton() {
        cancelDeleteButton.click();
        return this;
    }

    // Form input methods
    public PersonPage typeFirstName(String firstName) {
        firstNameInput.clear();
        firstNameInput.sendKeys(firstName);
        return this;
    }

    public PersonPage typeLastName(String lastName) {
        lastNameInput.clear();
        lastNameInput.sendKeys(lastName);
        return this;
    }

    public PersonPage typeEmail(String email) {
        emailInput.clear();
        emailInput.sendKeys(email);
        return this;
    }

    public PersonPage typeIdNumber(String idNumber) {
        idNumberInput.clear();
        idNumberInput.sendKeys(idNumber);
        return this;
    }

    public PersonPage clearAndTypeFirstName(String firstName) {
        firstNameInput.clear();
        firstNameInput.sendKeys(firstName);
        return this;
    }

    public PersonPage selectTitle(String titleValue) {
        titleSelect.selectOption(titleValue);
        return this;
    }

    public PersonPage selectGender(String genderValue) {
        genderSelect.selectOption(genderValue);
        return this;
    }

    public PersonPage selectIdType(String idTypeValue) {
        idTypeSelect.selectOption(idTypeValue);
        return this;
    }

    public PersonPage setActive(boolean active) {
        if (active) {
            isActiveCheckbox.setSelected(true);
        } else {
            isActiveCheckbox.setSelected(false);
        }
        return this;
    }

    // Table interaction methods
    public boolean hasPersonWithEmail(String email) {
        return tableRows.filterBy(text(email)).size() > 0;
    }

    public SelenideElement getRowByEmail(String email) {
        return tableRows.filterBy(text(email)).first();
    }

    public SelenideElement getRowById(Long personId) {
        return $("tr[data-person-id='" + personId + "']");
    }

    // Getter methods
    public SelenideElement getContentArea() {
        return contentArea;
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

    public SelenideElement getFirstNameField() {
        return firstNameInput;
    }

    public SelenideElement getLastNameField() {
        return lastNameInput;
    }

    public SelenideElement getEmailField() {
        return emailInput;
    }

    public SelenideElement getFirstNameValue() {
        return firstNameValue;
    }

    public SelenideElement getLastNameValue() {
        return lastNameValue;
    }

    public SelenideElement getEmailValue() {
        return emailValue;
    }

    public SelenideElement getBackButton() {
        return backButton;
    }

    public SelenideElement getDeleteModal() {
        return deleteModal;
    }

    public SelenideElement getAlertMessage() {
        return alertMessage;
    }

    // Validation methods
    public boolean isTableVisible() {
        return personTable.isDisplayed();
    }

    public boolean isViewMode() {
        // In view mode, form inputs should be disabled or readonly
        return firstNameValue.exists() && lastNameValue.exists() && emailValue.exists();
    }

    public boolean hasValidationErrors() {
        return validationErrors.size() > 0 || alertMessage.exists();
    }

    public String getPersonTableText() {
        return personTable.getText();
    }

    public String getFirstNameText() {
        return firstNameInput.getValue();
    }

    public String getLastNameText() {
        return lastNameInput.getValue();
    }

    public String getEmailText() {
        return emailInput.getValue();
    }
}