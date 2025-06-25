package io.archton.scaffold.e2e.pages;

import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selectors.*;
import static com.codeborne.selenide.Selenide.*;

/**
 * Simplified Page Object following Selenide best practices.
 * Focus on essential actions and verifications, letting Selenide handle waits.
 */
public class GenderPage {

    // Essential element selectors only
    public SelenideElement contentArea() { return $("#content-area"); }
    public SelenideElement gendersTable() { return $("table.table"); }
    public SelenideElement pageTitle() { return $("h1"); }
    public SelenideElement createButton() { return $("#create-new-btn"); }

    // Form elements
    public SelenideElement codeInput() { return $("#code"); }
    public SelenideElement descriptionInput() { return $("#description"); }
    public SelenideElement isActiveCheckbox() { return $("#isActive"); }

    // Action buttons
    public SelenideElement submitCreateButton() { return $("#submit-create-btn"); }
    public SelenideElement cancelCreateButton() { return $("#cancel-create-btn"); }
    public SelenideElement submitEditButton() { return $("#submit-edit-btn"); }
    public SelenideElement cancelEditButton() { return $("#cancel-edit-btn"); }
    public SelenideElement confirmDeleteButton() { return $("#confirm-delete-btn"); }

    public GenderPage openPage() {
        open("/genders-ui");
        return this;
    }

    // Streamlined methods using Selenide's fluent API
    public GenderPage createGender(String code, String description) {
        createButton().click();
        codeInput().setValue(code);
        descriptionInput().setValue(description);
        submitCreateButton().click();
        return this;
    }

    public GenderPage editGender(String currentCode, String newCode, String newDescription) {
        getRowByCode(currentCode).$("button", 1).click(); // Edit button is second
        codeInput().clear();
        codeInput().setValue(newCode);
        descriptionInput().clear();
        descriptionInput().setValue(newDescription);
        submitEditButton().click();
        return this;
    }

    public GenderPage deleteGender(String code) {
        getRowByCode(code).$("button", 2).click(); // Delete button is third
        confirmDeleteButton().click();
        return this;
    }

    public GenderPage viewGender(String code) {
        getRowByCode(code).$("button", 0).click(); // View button is first
        return this;
    }

    // Simplified element finding
    public SelenideElement getRowByCode(String code) {
        return $$("td.fw-bold").findBy(text(code)).closest("tr");
    }

    // Essential verification methods
    public GenderPage shouldHaveGender(String code) {
        $$("td.fw-bold").findBy(text(code)).shouldBe(visible);
        return this;
    }

    public GenderPage shouldNotHaveGender(String code) {
        $$("td.fw-bold").findBy(text(code)).shouldNot(exist);
        return this;
    }

    public GenderPage shouldShowActiveStatus(String code, boolean isActive) {
        var row = getRowByCode(code);
        if (isActive) {
            row.shouldHave(text("Active"));
        } else {
            row.shouldHave(text("Inactive"));
        }
        return this;
    }
}