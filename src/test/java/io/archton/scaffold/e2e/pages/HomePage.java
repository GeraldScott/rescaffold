package io.archton.scaffold.e2e.pages;

import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;

public class HomePage {
    
    // Navigation elements
    private final SelenideElement homeNavLink = $("a.nav-link[href='/']");
    private final SelenideElement maintenanceDropdown = $("#maintenanceDropdown");
    private final SelenideElement navbar = $("nav.navbar");
    private final SelenideElement navbarBrand = $("a.navbar-brand");
    private final SelenideElement gendersDropdownLink = $("a.dropdown-item[href='/genders-ui']");
    private final SelenideElement titlesDropdownLink = $("a.dropdown-item[href='/titles-ui']");
    
    public HomePage openPage() {
        open("/");
        return this;
    }
    
    public SelenideElement getHomeNavLink() {
        return homeNavLink;
    }
    
    public SelenideElement getMaintenanceDropdown() {
        return maintenanceDropdown;
    }
    
    public SelenideElement getNavbar() {
        return navbar;
    }
    
    public SelenideElement getNavbarBrand() {
        return navbarBrand;
    }
    
    public boolean isNavbarVisible() {
        return navbar.isDisplayed();
    }
    
    public boolean isHomeNavLinkVisible() {
        return homeNavLink.isDisplayed();
    }
    
    public boolean isMaintenanceDropdownVisible() {
        return maintenanceDropdown.isDisplayed();
    }
    
    public String getNavbarBrandText() {
        return navbarBrand.getText();
    }
    
    public String getHomeNavLinkText() {
        return homeNavLink.getText();
    }
    
    public String getMaintenanceDropdownText() {
        return maintenanceDropdown.getText();
    }
    
    public SelenideElement getGendersDropdownLink() {
        return gendersDropdownLink;
    }
    
    public SelenideElement getTitlesDropdownLink() {
        return titlesDropdownLink;
    }

    public HomePage clickMaintenanceDropdown() {
        maintenanceDropdown.click();
        return this;
    }
    
    public HomePage clickGendersLink() {
        gendersDropdownLink.click();
        return this;
    }
    
    public HomePage clickTitlesLink() {
        titlesDropdownLink.click();
        return this;
    }

    public boolean isGendersDropdownLinkVisible() {
        return gendersDropdownLink.isDisplayed();
    }

    public boolean isTitlesDropdownLinkVisible() {
        return titlesDropdownLink.isDisplayed();
    }
}