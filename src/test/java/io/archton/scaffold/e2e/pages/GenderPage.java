package io.archton.scaffold.e2e.pages;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.Selenide.open;

public class GenderPage {
    
    // Page elements
    private final SelenideElement pageTitle = $("h1");
    private final SelenideElement genderTable = $("table.table");
    private final ElementsCollection tableHeaders = $$("table.table th");
    private final SelenideElement createButton = $("button[hx-get='/genders-ui/create']");
    
    // Table row related elements
    private final ElementsCollection tableRows = $$("table.table tbody tr");
    private final ElementsCollection viewButtons = $$("button[id^='view-btn-']");
    private final ElementsCollection editButtons = $$("button[id^='edit-btn-']");
    private final ElementsCollection deleteButtons = $$("button[id^='delete-btn-']");
    
    // Form elements for create/edit
    private final SelenideElement codeInput = $("#code");
    private final SelenideElement descriptionInput = $("#description");
    private final SelenideElement saveButton = $("button[type='submit']");
    private final SelenideElement cancelButton = $("button.btn-outline-secondary[hx-get='/genders-ui/table']");
    private final SelenideElement errorMessage = $(".alert-danger");
    
    // Detail view elements
    private final SelenideElement detailCard = $(".card");
    private final SelenideElement backButton = $("button.btn-outline-secondary");
    
    /**
     * Opens the gender page directly
     * @return this page object for method chaining
     */
    public GenderPage openPage() {
        open("/genders-ui");
        return this;
    }
    
    /**
     * Getters for page elements
     */
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
    
    /**
     * Clicks the create button to navigate to the create gender form
     * @return this page object for method chaining
     */
    public GenderPage clickCreate() {
        createButton.click();
        return this;
    }
    
    /**
     * Fills out the gender form with the provided values
     * @param code The gender code
     * @param description The gender description
     * @return this page object for method chaining
     */
    public GenderPage fillGenderForm(String code, String description) {
        codeInput.setValue(code);
        descriptionInput.setValue(description);
        return this;
    }
    
    /**
     * Clicks the save button to submit the form
     * @return this page object for method chaining
     */
    public GenderPage clickSave() {
        saveButton.click();
        return this;
    }
    
    /**
     * Clicks the cancel button to return to the gender table view
     * @return this page object for method chaining
     */
    public GenderPage clickCancel() {
        cancelButton.click();
        return this;
    }
    
    /**
     * Gets the view button for a specific gender row by index
     * @param index The zero-based index of the row
     * @return The view button element
     */
    public SelenideElement getViewButton(int index) {
        return viewButtons.get(index);
    }
    
    /**
     * Gets the edit button for a specific gender row by index
     * @param index The zero-based index of the row
     * @return The edit button element
     */
    public SelenideElement getEditButton(int index) {
        return editButtons.get(index);
    }
    
    /**
     * Gets the delete button for a specific gender row by index
     * @param index The zero-based index of the row
     * @return The delete button element
     */
    public SelenideElement getDeleteButton(int index) {
        return deleteButtons.get(index);
    }
    
    /**
     * Gets the error message element that appears on validation errors
     * @return The error message element
     */
    public SelenideElement getErrorMessage() {
        return errorMessage;
    }
    
    /**
     * Gets the detail card element that appears in view mode
     * @return The detail card element
     */
    public SelenideElement getDetailCard() {
        return detailCard;
    }
    
    /**
     * Gets the back button element
     * @return The back button element
     */
    public SelenideElement getBackButton() {
        return backButton;
    }
}