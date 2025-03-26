package com.tus.finance.selenium;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UITests {
    private WebDriver driver;
    private WebDriverWait wait;
    private final String BASE_URL = "http://localhost:9091";

    @BeforeAll
    void setup() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    @BeforeEach
    void beforeEach() {
        driver.get(BASE_URL + "/index.html");
        wait.until(ExpectedConditions.jsReturnsValue("return document.readyState === 'complete'"));
    }

    // Helper methods
    private void login(String email, String password) {
        try {
            // Wait for page to be fully interactive
            wait.until(ExpectedConditions.jsReturnsValue("return document.readyState === 'complete'"));
            
            // Alternative: Wait for any critical element that indicates page is ready
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("body")));
            
            // Debug current state
            System.out.println("Current URL: " + driver.getCurrentUrl());
            System.out.println("Page source: " + driver.getPageSource().substring(0, 500) + "..."); // First 500 chars
            
            // More flexible element location - try multiple selectors
            WebElement username = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("input[id='loginEmail'], input[name='email'], [data-testid='email']")));
            
            WebElement passwordField = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("input[id='loginPassword'], input[name='password'], [data-testid='password']")));
            
            // Try multiple ways to locate the login button
            WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("button#LoginButton, input#LoginButton, [data-testid='login-btn'], button[type='submit']")));
            
            username.clear();
            username.sendKeys(email);
            passwordField.clear();
            passwordField.sendKeys(password);
            
            // Scroll into view and click using JavaScript as fallback
            ((JavascriptExecutor)driver).executeScript("arguments[0].scrollIntoView(true);", loginButton);
            ((JavascriptExecutor)driver).executeScript("arguments[0].click();", loginButton);
            
            // Wait for login to complete
            wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("login")));
            
        } catch (Exception e) {
            takeScreenshot("login_error");
            throw new RuntimeException("Login failed: " + e.getMessage(), e);
        }
    }

    private void logout() {
        driver.get(BASE_URL + "/logout");
        wait.until(ExpectedConditions.urlContains("login"));
    }

    private void takeScreenshot(String testName) {
        try {
            File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            FileUtils.copyFile(screenshot, new File("target/screenshots/" + testName + ".png"));
        } catch (IOException e) {
            System.err.println("Failed to take screenshot: " + e.getMessage());
        }
    }

    // Regular user tests
    @Test
    @Order(1)
    void testRegularUserLogin() {
        try {
            // Navigate to login page
            driver.get(BASE_URL + "/index.html#login");
            
            // Wait for page to fully load (including JavaScript)
            wait.until(webDriver -> ((JavascriptExecutor) webDriver)
                .executeScript("return document.readyState").equals("complete"));
            
            // Debug: Print page title and current URL
            System.out.println("Page title: " + driver.getTitle());
            System.out.println("Current URL: " + driver.getCurrentUrl());
            
            // Wait for login form to be visible
            WebElement loginForm = wait.until(ExpectedConditions
                .visibilityOfElementLocated(By.id("loginForm")));
            
            // Find elements within the form
            WebElement emailField = loginForm.findElement(By.id("loginEmail"));
            WebElement passwordField = loginForm.findElement(By.id("loginPassword"));
            WebElement loginButton = loginForm.findElement(By.id("loginButton"));
            
            // Verify elements are interactable
            assertTrue(emailField.isDisplayed(), "Email field should be visible");
            assertTrue(emailField.isEnabled(), "Email field should be enabled");
            assertTrue(passwordField.isDisplayed(), "Password field should be visible");
            assertTrue(passwordField.isEnabled(), "Password field should be enabled");
            assertTrue(loginButton.isDisplayed(), "Login button should be visible");
            assertTrue(loginButton.isEnabled(), "Login button should be enabled");
            
            // Enter credentials
            emailField.clear();
            emailField.sendKeys("keerthi@example.com");
            passwordField.clear();
            passwordField.sendKeys("keerthi123");
            
            // Scroll button into view (in case it's not visible)
            ((JavascriptExecutor)driver).executeScript(
                "arguments[0].scrollIntoView({block: 'center'});", loginButton);
            
            // Click using JavaScript as a reliable alternative
            ((JavascriptExecutor)driver).executeScript("arguments[0].click();", loginButton);
            
            // Wait for navigation or some post-login element
            try {
                wait.until(ExpectedConditions.or(
                    ExpectedConditions.urlContains("dashboard"),
                    ExpectedConditions.presenceOfElementLocated(By.id("userDashboard"))
                ));
            } catch (TimeoutException e) {
                // Check for error message if login failed
                WebElement alert = driver.findElement(By.id("loginAlert"));
                if (!alert.getAttribute("class").contains("d-none")) {
                    fail("Login failed with error: " + alert.getText());
                }
                throw e;
            }
            
            // Verification after login
            assertTrue(driver.getCurrentUrl().contains("dashboard") || 
                     driver.findElements(By.id("userDashboard")).size() > 0,
                     "Should be redirected after login");
            
        } catch (Exception e) {
            takeScreenshot("login_failure");
            throw new RuntimeException("Login test failed", e);
        }
    }

    @Test
    @Order(2)
    void testTransactionHistory() {
        login("keerthi@example.com", "keerthi123");
        
        // More robust wait for dashboard
        wait.until(ExpectedConditions.or(
            ExpectedConditions.urlContains("dashboard"),
            ExpectedConditions.presenceOfElementLocated(By.cssSelector(".user-dashboard"))
        ));
        
        // Click transaction tab with JS and wait for navigation
        WebElement transactionHistoryTab = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='transactions'], [data-testid='transactions-tab']")));
        ((JavascriptExecutor)driver).executeScript("arguments[0].click();", transactionHistoryTab);
        
        // Wait specifically for transaction page elements
        wait.until(ExpectedConditions.or(
            ExpectedConditions.urlContains("transactions"),
            ExpectedConditions.presenceOfElementLocated(By.id("transactionTable"))
        ));
        
        // Additional wait for data loading
        wait.until(d -> {
            List<WebElement> rows = d.findElements(By.cssSelector("#transactionTable tbody tr"));
            return !rows.isEmpty() || 
                   d.findElements(By.cssSelector(".loading-indicator")).isEmpty();
        });
        
        // Verify page
        assertTrue(driver.getCurrentUrl().contains("transactions") || 
                 driver.findElements(By.id("transactionTable")).size() > 0,
                 "Should be on transactions page");
    }

   
    @Test
    @Order(3)
    void testDeleteTransaction() {
        login("keerthi@example.com", "keerthi123");
        
        // Navigate to transactions using SPA navigation
        WebElement transactionTab = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='transactions']")));
        ((JavascriptExecutor)driver).executeScript("arguments[0].click();", transactionTab);
        
        // Wait for table to load
        wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(
            By.cssSelector("#transactionTableBody tr"), 0));
        
        // Get initial row count
        List<WebElement> initialRows = driver.findElements(
            By.cssSelector("#transactionTableBody tr"));
        int initialRowCount = initialRows.size();
        
        if (initialRowCount == 0) {
            fail("No transactions available to delete");
        }
        
        // Click delete button on first row
        WebElement firstDeleteButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("#transactionTableBody tr:first-child button"))); // Simplified selector
        ((JavascriptExecutor)driver).executeScript("arguments[0].click();", firstDeleteButton);
        
        // Handle confirmation dialog if present
        try {
            Alert confirmDialog = wait.until(ExpectedConditions.alertIsPresent());
            confirmDialog.accept();
        } catch (TimeoutException e) {
            // If no alert, maybe a modal confirmation - adjust accordingly
            WebElement confirmButton = driver.findElement(By.cssSelector(".modal-footer .btn-primary"));
            confirmButton.click();
        }
        
        // Wait for row count to decrease
        wait.until(ExpectedConditions.numberOfElementsToBe(
            By.cssSelector("#transactionTableBody tr"), initialRowCount - 1));
        
        takeScreenshot("after_transaction_deletion");
    }
    @Test
    @Order(4)
    void testNewTransaction() {
        login("keerthi@example.com", "keerthi123");
        
        // Navigate to new transaction page using SPA navigation
        WebElement newTransactionTab = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='new-transaction'], [data-testid='new-transaction']")));
        ((JavascriptExecutor)driver).executeScript("arguments[0].click();", newTransactionTab);
        
        // Wait for form to load
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("transactionForm")));
        
        // Fill out form
        WebElement amount = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("#amount")));
        WebElement transactionType = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("#type"))); // Changed from #transactionType to #type
        WebElement category = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("#category")));
        WebElement date = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("#date")));
        WebElement saveButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("#transactionForm button[type='submit']")));

        amount.clear();
        amount.sendKeys("50.00");
        
        new Select(transactionType).selectByVisibleText("Debit");
        new Select(category).selectByVisibleText("Food");
        date.sendKeys("2024-03-20");
        
        // Scroll and click using JavaScript
        ((JavascriptExecutor)driver).executeScript("arguments[0].scrollIntoView(true);", saveButton);
        ((JavascriptExecutor)driver).executeScript("arguments[0].click();", saveButton);
        
        // Wait for success message or redirect
        try {
            wait.until(ExpectedConditions.or(
                ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector(".alert-success")),
                ExpectedConditions.urlContains("transactions"))
            );
        } catch (TimeoutException e) {
            // Check for error message
            WebElement alert = driver.findElement(By.id("alertMessage"));
            if (alert.isDisplayed()) {
                fail("Transaction failed with error: " + alert.getText());
            }
            throw e;
        }
        
        takeScreenshot("new_transaction_created");
    }
    @Test
    @Order(5)
    void testAdminLogin() {
        // Clear cookies first
        driver.manage().deleteAllCookies();
        driver.get(BASE_URL + "/index.html#login");
        
        // Admin-specific login
        WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("input[id='loginEmail']")));
        WebElement passwordField = driver.findElement(By.cssSelector("input[id='loginPassword']"));
        WebElement loginButton = driver.findElement(By.cssSelector("button#LoginButton"));
        
        emailField.clear();
        emailField.sendKeys("vel@example.com");
        passwordField.clear();
        passwordField.sendKeys("vel123");
        
        ((JavascriptExecutor)driver).executeScript("arguments[0].click();", loginButton);
        
        // Special wait for admin dashboard
        wait.until(ExpectedConditions.or(
            ExpectedConditions.urlContains("admin"),
            ExpectedConditions.presenceOfElementLocated(By.cssSelector(".admin-dashboard")),
            ExpectedConditions.presenceOfElementLocated(By.id("adminUsersMenu"))
        ));
        
        // Additional verification
        try {
            WebElement adminDashboard = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("h2.admin-header, .admin-dashboard")));
            assertTrue(adminDashboard.getText().contains("Admin"));
        } catch (TimeoutException e) {
            takeScreenshot("admin_login_failed");
            fail("Admin dashboard not loaded. Current page: " + driver.getCurrentUrl());
        }
    }

    @Test
    @Order(6)
    void testAdminDisableUser() {
        login("vel@example.com", "vel123");
        
        // Navigate to users page
        WebElement usersTab = wait.until(ExpectedConditions.elementToBeClickable(By.id("adminUsersMenu")));
        usersTab.click();
        
        // Find and disable user
        WebElement disableButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//tr[td[contains(text(),'kala@example.com')]]//button[contains(text(),'Disable')]")));
        disableButton.click();
        
        // Verify status change
        WebElement confirmation = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".alert-success")));
        assertTrue(confirmation.getText().contains("User status updated"));
        
        // Verify status in table
        WebElement statusCell = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//tr[td[contains(text(),'kala@example.com')]]/td[contains(@class,'status')]")));
        assertTrue(statusCell.getText().contains("Disabled"));
        
        takeScreenshot("user_disabled");
    }

    @AfterEach
    void tearDown() {
        // Logout after each test
        try {
            driver.manage().deleteAllCookies();
            driver.get(BASE_URL + "/logout"); // If you have a logout endpoint
            // Or alternatively refresh the page
            driver.navigate().refresh();
        } catch (Exception e) {
            // Ignore cleanup errors
        }
    }

    @AfterAll
    void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }
}