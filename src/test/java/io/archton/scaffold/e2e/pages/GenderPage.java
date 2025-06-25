package io.archton.scaffold.e2e.pages;

import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selectors.*;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;

public class GenderPage {
    
    // Table elements
    private final SelenideElement contentArea = $("#content-area");
    private final SelenideElement gendersTable = $("table.table");
    private final SelenideElement tableBody = $("tbody");
    private final SelenideElement pageTitle = $("h1");
    
    // Action buttons in table
    private final SelenideElement createNewButton = $("#create-new-btn");
    
    // Form elements
    private final SelenideElement codeInput = $("#code");
    private final SelenideElement descriptionInput = $("#description");
    private final SelenideElement isActiveCheckbox = $("#isActive");
    private final SelenideElement createForm = $("#create-form");
    private final SelenideElement editForm = $("#edit-form");
    
    // Form action buttons
    private final SelenideElement submitCreateButton = $("#submit-create-btn");
    private final SelenideElement cancelCreateButton = $("#cancel-create-btn");
    private final SelenideElement submitEditButton = $("#submit-edit-btn");
    private final SelenideElement cancelEditButton = $("#cancel-edit-btn");
    private final SelenideElement confirmDeleteButton = $("#confirm-delete-btn");
    private final SelenideElement cancelDeleteButton = $("#cancel-delete-btn");
    
    public GenderPage openPage() {
        open("/genders-ui");
        return this;
    }
    
    // Table verification methods
    public SelenideElement getContentArea() {
        return contentArea;
    }
    
    public SelenideElement getGendersTable() {
        return gendersTable;
    }
    
    public SelenideElement getPageTitle() {
        return pageTitle;
    }
    
    public boolean isTableVisible() {
        return gendersTable.isDisplayed();
    }
    
    public boolean isContentAreaVisible() {
        return contentArea.isDisplayed();
    }
    
    // Navigation and CRUD action methods
    public GenderPage clickCreateNew() {
        createNewButton.click();
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
    
    // Form interaction methods
    public GenderPage fillCreateForm(String code, String description) {
        codeInput.setValue(code);
        descriptionInput.setValue(description);
        return this;
    }
    
    public GenderPage fillEditForm(String code, String description) {
        codeInput.clear();
        codeInput.setValue(code);
        descriptionInput.clear();
        descriptionInput.setValue(description);
        return this;
    }
    
    public GenderPage fillEditForm(String code, String description, boolean isActive) {
        codeInput.clear();
        codeInput.setValue(code);
        descriptionInput.clear();
        descriptionInput.setValue(description);
        setIsActiveCheckbox(isActive);
        return this;
    }
    
    public GenderPage setIsActiveCheckbox(boolean isActive) {
        if (isActive && !isActiveCheckbox.isSelected()) {
            isActiveCheckbox.click();
        } else if (!isActive && isActiveCheckbox.isSelected()) {
            isActiveCheckbox.click();
        }
        return this;
    }
    
    public GenderPage submitCreateForm() {
        submitCreateButton.click();
        return this;
    }
    
    public GenderPage submitEditForm() {
        submitEditButton.click();
        return this;
    }
    
    public GenderPage cancelCreate() {
        cancelCreateButton.click();
        return this;
    }
    
    public GenderPage cancelEdit() {
        cancelEditButton.click();
        return this;
    }
    
    public GenderPage confirmDelete() {
        confirmDeleteButton.click();
        return this;
    }
    
    public GenderPage cancelDelete() {
        cancelDeleteButton.click();
        return this;
    }
    
    // Verification methods
    public String getCurrentCode() {
        return codeInput.getValue();
    }
    
    public String getCurrentDescription() {
        return descriptionInput.getValue();
    }
    
    public boolean isCreateFormVisible() {
        return createForm.exists();
    }
    
    public boolean isEditFormVisible() {
        return editForm.exists();
    }
    
    public boolean hasRowWithCode(String code) {
        try {
            return getRowByCode(code).exists();
        } catch (Exception e) {
            return false;
        }
    }
    
    public SelenideElement getRowByCode(String code) {
        // Find the table row that contains the code in the first cell (fw-bold class)
        return $(byText(code)).closest("tr");
    }
    
    public boolean isDeleteConfirmationVisible() {
        return $(".alert-warning").exists();
    }
    
    // Get form elements for direct access
    public SelenideElement getCodeInput() {
        return codeInput;
    }
    
    public SelenideElement getDescriptionInput() {
        return descriptionInput;
    }
    
    public SelenideElement getIsActiveCheckbox() {
        return isActiveCheckbox;
    }
    
    public boolean isActiveCheckboxSelected() {
        return isActiveCheckbox.isSelected();
    }
    
    public SelenideElement getCreateNewButton() {
        return createNewButton;
    }
    
    public SelenideElement getSubmitCreateButton() {
        return submitCreateButton;
    }
    
    public SelenideElement getSubmitEditButton() {
        return submitEditButton;
    }
    
    public SelenideElement getConfirmDeleteButton() {
        return confirmDeleteButton;
    }
}