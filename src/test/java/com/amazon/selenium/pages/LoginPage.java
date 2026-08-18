package com.amazon.selenium.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * Page object for the login screen.
 *
 * <p>Modeled against the public practice site
 * {@code https://the-internet.herokuapp.com/login} so the sample tests run
 * against a real, stable target. Swap the locators/URL for your application
 * under test.</p>
 */
public class LoginPage extends BasePage {

    public static final String PATH = "/login";

    private static final By USERNAME = By.id("username");
    private static final By PASSWORD = By.id("password");
    private static final By SUBMIT = By.cssSelector("button[type='submit']");
    private static final By FLASH_MESSAGE = By.id("flash");

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    public LoginPage enterUsername(String username) {
        type(USERNAME, username);
        return this;
    }

    public LoginPage enterPassword(String password) {
        type(PASSWORD, password);
        return this;
    }

    /**
     * Submits valid credentials and returns the resulting dashboard page.
     */
    public DashboardPage loginExpectingSuccess(String username, String password) {
        enterUsername(username);
        enterPassword(password);
        click(SUBMIT);
        return new DashboardPage(driver);
    }

    /**
     * Submits credentials expected to fail; the page stays on login.
     */
    public LoginPage loginExpectingFailure(String username, String password) {
        enterUsername(username);
        enterPassword(password);
        click(SUBMIT);
        return this;
    }

    public boolean isLoaded() {
        return isDisplayed(USERNAME) && isDisplayed(SUBMIT);
    }

    public String getFlashMessage() {
        return getText(FLASH_MESSAGE).trim();
    }
}
