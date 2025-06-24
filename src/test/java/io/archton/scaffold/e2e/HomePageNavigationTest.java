package io.archton.scaffold.e2e;

import io.archton.scaffold.e2e.base.BaseSelenideTest;
import io.archton.scaffold.e2e.pages.HomePage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.*;
import static com.codeborne.selenide.WebDriverConditions.url;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Home Page Navigation E2E Tests")
class HomePageNavigationTest extends BaseSelenideTest {
    
    private final HomePage homePage = new HomePage();
    
    @Test
    @DisplayName("Should load home page successfully")
    void shouldLoadHomePageSuccessfully() {
        homePage.openPage();
        
        // Verify the page loads and navbar is visible
        homePage.getNavbar().should(exist);
        homePage.getNavbar().should(be(visible));
        
        assertTrue(homePage.isNavbarVisible(), "Navbar should be visible");
    }
    
    @Test
    @DisplayName("Should display Home navigation link")
    void shouldDisplayHomeNavigationLink() {
        homePage.openPage();
        
        // Verify Home nav link exists and is visible
        homePage.getHomeNavLink().should(exist);
        homePage.getHomeNavLink().should(be(visible));
        homePage.getHomeNavLink().should(have(text("Home")));
        
        assertTrue(homePage.isHomeNavLinkVisible(), "Home navigation link should be visible");
    }
    
    @Test
    @DisplayName("Should display Maintenance dropdown")
    void shouldDisplayMaintenanceDropdown() {
        homePage.openPage();
        
        // Verify Maintenance dropdown exists and is visible
        homePage.getMaintenanceDropdown().should(exist);
        homePage.getMaintenanceDropdown().should(be(visible));
        homePage.getMaintenanceDropdown().should(have(text("Maintenance")));
        
        assertTrue(homePage.isMaintenanceDropdownVisible(), "Maintenance dropdown should be visible");
    }
    
    @Test
    @DisplayName("Should display navbar brand with Rescaffold text")
    void shouldDisplayNavbarBrand() {
        homePage.openPage();
        
        // Verify navbar brand exists and has correct text
        homePage.getNavbarBrand().should(exist);
        homePage.getNavbarBrand().should(be(visible));
        homePage.getNavbarBrand().should(have(text("Rescaffold")));
    }
    
    @Test
    @DisplayName("Should have all required navigation elements")
    void shouldHaveAllRequiredNavigationElements() {
        homePage.openPage();
        
        // Comprehensive check for all navigation elements
        homePage.getNavbar().should(exist);
        homePage.getNavbarBrand().should(exist);
        homePage.getHomeNavLink().should(exist);
        homePage.getMaintenanceDropdown().should(exist);
        
        // Verify all elements are visible
        assertTrue(homePage.isNavbarVisible(), "Navbar should be visible");
        assertTrue(homePage.isHomeNavLinkVisible(), "Home nav link should be visible");
        assertTrue(homePage.isMaintenanceDropdownVisible(), "Maintenance dropdown should be visible");
        
        // Verify text content
        assertTrue(homePage.getNavbarBrandText().contains("Rescaffold"), "Navbar brand should contain 'Rescaffold'");
        assertTrue(homePage.getHomeNavLinkText().contains("Home"), "Home nav link should contain 'Home'");
        assertTrue(homePage.getMaintenanceDropdownText().contains("Maintenance"), "Maintenance dropdown should contain 'Maintenance'");
    }
    
    @Test
    @DisplayName("Should navigate to Genders from Maintenance dropdown")
    void shouldNavigateToGendersFromMaintenanceDropdown() {
        homePage.openPage();
        
        // Click on Maintenance dropdown to reveal menu items
        homePage.clickMaintenanceDropdown();
        
        // Verify Genders link is visible in dropdown
        homePage.getGendersDropdownLink().should(exist);
        homePage.getGendersDropdownLink().should(be(visible));
        homePage.getGendersDropdownLink().should(have(text("Genders")));
        
        assertTrue(homePage.isGendersDropdownLinkVisible(), "Genders dropdown link should be visible");
        
        // Click Genders link
        homePage.clickGendersLink();
        
        // Verify navigation to Genders page
        webdriver().shouldHave(url(BASE_URL + "/genders-ui"));
        
        // Verify Gender CRUD page loads with table
        $("#content-area").should(exist);
        $("table.table").should(exist);
        $("h1").should(have(text("Genders")));
    }
}