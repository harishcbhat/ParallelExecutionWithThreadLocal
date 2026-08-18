package com.selenium.core;

import com.selenium.pages.DashboardPage;
import com.selenium.pages.LoginPage;
import org.openqa.selenium.WebDriver;

/**
 * Thread-safe accessor for page objects.
 *
 * <p>Page objects are cheap and stateless beyond the driver they wrap, so each
 * accessor simply constructs one over the current thread's driver (fetched from
 * {@link DriverFactory}). Because the driver is thread-confined, the page
 * objects produced here are too — nothing is shared across threads.</p>
 *
 * <p>Named {@code PageFactory} to match the project layout; note this is our own
 * lightweight factory, distinct from Selenium's
 * {@code org.openqa.selenium.support.PageFactory}.</p>
 */
public final class PageFactory {

    private PageFactory() {
        // Utility class: no instances.
    }

    private static WebDriver driver() {
        return DriverFactory.getDriver();
    }

    public static LoginPage loginPage() {
        return new LoginPage(driver());
    }

    public static DashboardPage dashboardPage() {
        return new DashboardPage(driver());
    }
}
