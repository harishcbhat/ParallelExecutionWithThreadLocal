# Thread-Safe Selenium POM (Parallel Execution with ThreadLocal)

A small Selenium + TestNG framework that demonstrates **thread-safe parallel test
execution** using the Page Object Model (POM). Each test thread gets its own
isolated `WebDriver` via a `ThreadLocal`, so the exact same suite can run either
in **parallel** or **serially** with zero code changes — you just pick the suite.

## Tech stack

| Tool | Version | Purpose |
|------|---------|---------|
| Java | 17 | Language |
| Selenium | 4.47.0 | Browser automation |
| TestNG | 7.9.0 | Test framework + parallel runner |
| WebDriverManager | 5.8.0 | Auto-downloads the browser driver binary |
| SLF4J (api + simple) | 2.0.13 | Logging |
| Maven Surefire | 3.2.5 | Runs the TestNG suite |

## Project structure

```
.
├── pom.xml                    # Build + dependencies; parameterized for CLI flags
├── testng.xml                 # Parallel suite (parallel="methods", thread-count=3)
├── testng-serial.xml          # Serial suite (no parallel attribute -> single thread)
└── src/test
    ├── java/com/amazon/selenium
    │   ├── core
    │   │   ├── DriverFactory.java   # ThreadLocal<WebDriver> lifecycle (the core)
    │   │   └── PageFactory.java     # Builds page objects over the thread's driver
    │   ├── pages
    │   │   ├── BasePage.java        # Shared waits + interaction helpers
    │   │   ├── LoginPage.java       # Login page object
    │   │   └── DashboardPage.java   # Secure-area page object
    │   └── tests
    │       ├── BaseTest.java        # @BeforeMethod/@AfterMethod driver lifecycle
    │       └── LoginTest.java       # 4 login tests
    └── resources
        └── simplelogger.properties  # Clean, thread-named log output
```

## How thread-safety works

The whole design hinges on one idea: **never share a `WebDriver` across threads.**

- `DriverFactory` stores the driver in a `ThreadLocal<WebDriver>`, so
  `getDriver()` always returns *the current thread's* driver.
- `BaseTest.@BeforeMethod` creates a driver and `@AfterMethod` quits it. Because
  TestNG runs each `@Test` (and its before/after hooks) on the same thread,
  every test method gets a fresh, isolated browser.
- `quitDriver()` calls `DRIVER.remove()` so that when TestNG reuses a pooled
  thread for the next test, it starts clean — no stale driver leaks in.

Since threads never touch each other's driver, `parallel="methods"` is safe and
just makes the same tests finish faster.

## Running the tests

The browser driver is downloaded automatically (WebDriverManager) — no manual
setup needed. You do need the browser itself installed (Chrome by default).

```bash
# Parallel (default): 3 threads, headless Chrome
mvn clean test

# Serial: everything on a single thread
mvn clean test -DsuiteXmlFile=testng-serial.xml
```

### Configuration flags

All flags are optional and can be combined. They are passed into the forked test
JVM via Surefire and read in `BaseTest`.

| Flag | Default | Example |
|------|---------|---------|
| `-Dbrowser` | `chrome` | `-Dbrowser=firefox` (also `edge`) |
| `-Dheadless` | `true` | `-Dheadless=false` (show the browser) |
| `-DbaseUrl` | `https://the-internet.herokuapp.com` | `-DbaseUrl=https://example.com` |
| `-DsuiteXmlFile` | `testng.xml` | `-DsuiteXmlFile=testng-serial.xml` |

```bash
# Serial run against a visible Chrome
mvn clean test -DsuiteXmlFile=testng-serial.xml -Dbrowser=chrome -Dheadless=false
```

## Parallel vs. serial (observed)

Same 4 tests, same passing result — parallel is just faster, which is exactly
what proves the ThreadLocal isolation is correct.

| Mode | Threads | Wall time (4 tests) | Result |
|------|---------|---------------------|--------|
| Serial | 1 (`main`) | ~30 s | 4/4 pass |
| Parallel | 3 (`TestNG-...-1/2/3`) | ~12 s | 4/4 pass |

You can see it in the logs — thread names appear on every line:

```
[TestNG-test-LoginTests-1] INFO DriverFactory - Created chrome driver (headless=true) for this thread
[TestNG-test-LoginTests-2] INFO DriverFactory - Created chrome driver (headless=true) for this thread
[TestNG-test-LoginTests-3] INFO DriverFactory - Created chrome driver (headless=true) for this thread
```

To change the parallel thread count, edit `thread-count` in `testng.xml`.

## Reports

After a run, reports are generated under `target/`:

- `target/surefire-reports/index.html` — TestNG dashboard
- `target/surefire-reports/emailable-report.html` — single-file summary
- `mvn surefire-report:report` → `target/reports/surefire.html`

```bash
open target/surefire-reports/index.html
```

## Notes

- The sample tests run against the public practice site
  `https://the-internet.herokuapp.com/login` (user `tomsmith` /
  `SuperSecretPassword!`). Swap the locators, URL, and credentials in the page
  objects and `LoginTest` for your own application under test.
- Firefox/Edge are wired in `DriverFactory` but only Chrome has been run here;
  using them requires that browser installed locally.
