package com.amazon.selenium.tests;

import com.amazon.selenium.core.PageFactory;
import com.amazon.selenium.pages.DashboardPage;
import com.amazon.selenium.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Login tests demonstrating thread-safe parallel execution.
 *
 * <p>Each method runs on its own thread with its own browser (see
 * {@code testng.xml} + {@link BaseTest}), so these can execute concurrently
 * without interfering with one another.</p>
 */
public class LoginTest extends BaseTest {

    private static final String VALID_USERNAME = "tomsmith";
    private static final String VALID_PASSWORD = "SuperSecretPassword!";

    @Test(description = "Valid credentials land the user on the secure dashboard")
    public void validLoginReachesDashboard() {
        LoginPage loginPage = PageFactory.loginPage();
        loginPage.open(baseUrl + LoginPage.PATH);
        Assert.assertTrue(loginPage.isLoaded(), "Login page did not load");

        DashboardPage dashboard =
                loginPage.loginExpectingSuccess(VALID_USERNAME, VALID_PASSWORD);

        Assert.assertTrue(dashboard.isLoaded(), "Dashboard did not load after login");
        Assert.assertTrue(dashboard.getFlashMessage().contains("You logged into a secure area!"),
                "Expected success flash message after valid login");
    }

    @Test(description = "Invalid password keeps the user on login with an error")
    public void invalidPasswordShowsError() {
        LoginPage loginPage = PageFactory.loginPage();
        loginPage.open(baseUrl + LoginPage.PATH);

        loginPage.loginExpectingFailure(VALID_USERNAME, "wrong-password");

        Assert.assertTrue(loginPage.getFlashMessage().contains("Your password is invalid!"),
                "Expected invalid-password error message");
    }

    @Test(description = "Unknown username keeps the user on login with an error")
    public void unknownUsernameShowsError() {
        LoginPage loginPage = PageFactory.loginPage();
        loginPage.open(baseUrl + LoginPage.PATH);

        loginPage.loginExpectingFailure("not-a-real-user", VALID_PASSWORD);

        Assert.assertTrue(loginPage.getFlashMessage().contains("Your username is invalid!"),
                "Expected invalid-username error message");
    }

    @Test(description = "Logging out returns the user to the login page")
    public void logoutReturnsToLogin() {
        LoginPage loginPage = PageFactory.loginPage();
        loginPage.open(baseUrl + LoginPage.PATH);

        DashboardPage dashboard =
                loginPage.loginExpectingSuccess(VALID_USERNAME, VALID_PASSWORD);
        Assert.assertTrue(dashboard.isLoaded(), "Dashboard did not load after login");

        LoginPage afterLogout = dashboard.logout();
        Assert.assertTrue(afterLogout.isLoaded(), "Did not return to login page after logout");
        Assert.assertTrue(afterLogout.getFlashMessage().contains("You logged out of the secure area!"),
                "Expected logout confirmation message");
    }
}
