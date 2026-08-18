package com.amazon.selenium.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * Page object for the secure area shown after a successful login.
 *
 * <p>Modeled against {@code https://the-internet.herokuapp.com/secure}.</p>
 */
public class DashboardPage extends BasePage {

    private static final By HEADING = By.cssSelector("h2");
    private static final By FLASH_MESSAGE = By.id("flash");
    private static final By LOGOUT_BUTTON = By.cssSelector("a.button");

    public DashboardPage(WebDriver driver) {
        super(driver);
    }

    /** @return {@code true} once the secure-area heading and logout control are visible. */
    public boolean isLoaded() {
        return isDisplayed(HEADING) && isDisplayed(LOGOUT_BUTTON);
    }

    public String getHeading() {
        return getText(HEADING).trim();
    }

    public String getFlashMessage() {
        return getText(FLASH_MESSAGE).trim();
    }

    public LoginPage logout() {
        click(LOGOUT_BUTTON);
        return new LoginPage(driver);
    }
}
