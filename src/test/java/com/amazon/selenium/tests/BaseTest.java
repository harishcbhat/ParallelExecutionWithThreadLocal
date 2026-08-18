package com.amazon.selenium.tests;

import com.amazon.selenium.core.DriverFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

/**
 * Base class for all tests.
 *
 * <p>Owns the per-thread WebDriver lifecycle. Because {@code testng.xml} uses
 * {@code parallel="methods"}, TestNG runs each {@code @Test} on its own thread
 * and drives {@code @BeforeMethod}/{@code @AfterMethod} on that same thread.
 * Creating and quitting the driver in these hooks therefore gives every test
 * method a fresh, thread-isolated browser via the {@link DriverFactory}
 * ThreadLocal.</p>
 */
public abstract class BaseTest {

    protected static final String DEFAULT_BASE_URL = "https://the-internet.herokuapp.com";

    protected String baseUrl;

    /**
     * Creates a browser for the current test thread.
     *
     * <p>Values resolve in order of precedence: TestNG suite parameters
     * (from {@code testng.xml}) provide defaults, and any matching system
     * property ({@code -Dbrowser=...}, {@code -Dheadless=...},
     * {@code -DbaseUrl=...}) overrides them, which is handy for CI.</p>
     */
    @BeforeMethod(alwaysRun = true)
    @Parameters({"browser", "headless"})
    public void setUp(@Optional("chrome") String browser,
                      @Optional("true") String headless) {

        String resolvedBrowser = System.getProperty("browser", browser);
        boolean resolvedHeadless =
                Boolean.parseBoolean(System.getProperty("headless", headless));
        this.baseUrl = System.getProperty("baseUrl", DEFAULT_BASE_URL);

        DriverFactory.createDriver(resolvedBrowser, resolvedHeadless);
    }

    /** Quits the current test thread's browser and clears its ThreadLocal entry. */
    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        DriverFactory.quitDriver();
    }
}
