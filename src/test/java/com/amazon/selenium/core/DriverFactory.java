package com.amazon.selenium.core;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

/**
 * Thread-safe provider of {@link WebDriver} instances.
 *
 * <p>The driver is held in a {@link ThreadLocal}, so every test thread gets its
 * own isolated browser session. This is what makes {@code parallel="methods"}
 * (see {@code testng.xml}) safe: no two threads ever share a driver.</p>
 *
 * <p>Usage from a test thread:</p>
 * <pre>
 *   DriverFactory.createDriver("chrome", true);   // once, in @BeforeMethod
 *   WebDriver driver = DriverFactory.getDriver();  // anywhere on the same thread
 *   DriverFactory.quitDriver();                    // once, in @AfterMethod
 * </pre>
 */
public final class DriverFactory {

    private static final Logger log = LoggerFactory.getLogger(DriverFactory.class);

    private static final ThreadLocal<WebDriver> DRIVER = new ThreadLocal<>();

    private static final Duration IMPLICIT_WAIT = Duration.ofSeconds(10);
    private static final Duration PAGE_LOAD_TIMEOUT = Duration.ofSeconds(30);

    private DriverFactory() {
        // Utility class: no instances.
    }

    /**
     * Creates a new browser session and binds it to the current thread.
     *
     * @param browser  browser name: {@code chrome}, {@code firefox} or {@code edge}
     * @param headless whether to run without a visible browser window
     */
    public static void createDriver(String browser, boolean headless) {
        if (DRIVER.get() != null) {
            // A driver already exists on this thread; avoid leaking a second one.
            return;
        }

        WebDriver driver = buildDriver(browser, headless);
        driver.manage().timeouts().implicitlyWait(IMPLICIT_WAIT);
        driver.manage().timeouts().pageLoadTimeout(PAGE_LOAD_TIMEOUT);
        driver.manage().window().maximize();

        DRIVER.set(driver);
        log.info("Created {} driver (headless={}) for this thread", browser, headless);
    }

    /**
     * @return the {@link WebDriver} bound to the current thread
     * @throws IllegalStateException if no driver has been created on this thread
     */
    public static WebDriver getDriver() {
        WebDriver driver = DRIVER.get();
        if (driver == null) {
            throw new IllegalStateException(
                    "No WebDriver for thread '" + Thread.currentThread().getName()
                            + "'. Call DriverFactory.createDriver(...) first.");
        }
        return driver;
    }

    /**
     * Quits the current thread's browser session and removes it from the
     * ThreadLocal. Always call this in teardown to avoid leaking sessions and
     * to prevent stale state bleeding into a thread's next test.
     */
    public static void quitDriver() {
        WebDriver driver = DRIVER.get();
        if (driver != null) {
            try {
                driver.quit();
                log.info("Quit driver for this thread");
            } finally {
                // Remove the entry so the pooled thread starts clean next time.
                DRIVER.remove();
            }
        }
    }

    private static WebDriver buildDriver(String browser, boolean headless) {
        String name = browser == null ? "chrome" : browser.trim().toLowerCase();

        switch (name) {
            case "firefox":
                WebDriverManager.firefoxdriver().setup();
                FirefoxOptions firefoxOptions = new FirefoxOptions();
                if (headless) {
                    firefoxOptions.addArguments("-headless");
                }
                return new FirefoxDriver(firefoxOptions);

            case "edge":
                WebDriverManager.edgedriver().setup();
                EdgeOptions edgeOptions = new EdgeOptions();
                if (headless) {
                    edgeOptions.addArguments("--headless=new");
                }
                return new EdgeDriver(edgeOptions);

            case "chrome":
            default:
                WebDriverManager.chromedriver().setup();
                ChromeOptions chromeOptions = new ChromeOptions();
                if (headless) {
                    chromeOptions.addArguments("--headless=new");
                }
                chromeOptions.addArguments("--no-sandbox");
                chromeOptions.addArguments("--disable-dev-shm-usage");
                chromeOptions.addArguments("--remote-allow-origins=*");
                return new ChromeDriver(chromeOptions);
        }
    }
}
